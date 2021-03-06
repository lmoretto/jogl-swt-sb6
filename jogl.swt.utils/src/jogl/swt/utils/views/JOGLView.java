package jogl.swt.utils.views;

import static javax.media.opengl.GL4.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.media.opengl.DebugGL4;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ProjectionType;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.math.MatrixUtils;
import jogl.swt.utils.math.Vec3f;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import com.jogamp.opengl.swt.GLCanvas;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.GLPixelBuffer;
import com.jogamp.opengl.util.GLPixelBuffer.GLPixelAttributes;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import org.eclipse.swt.widgets.Label;

public abstract class JOGLView extends ViewPart implements GLEventListener, KeyListener{
	//Viewport value
	private int lowerLeftX;
	private int lowerLeftY;
	private int width;
	private int height;
	
	private long initTime;
	
	private Matrix4f perspective;
	
	private Vec3f camera;
	private Vec3f target;
	private Vec3f up;
	private int lastAngleUpDown;
	private int lastAngleLeftRight;
	private Matrix4f lookAt;
	private Vec3f axisCamera;
	private Matrix4f axisLookAt;
	
	//frame rate
	private Label fpsLabel;
	private boolean started = false;
	private int numFrames;
	private double lastTime;
	
	//Queries
	// - pipeline latency
	// - rendering time
	// - primitive count
	// - transform feedback written primitives
	private int[] queries = new int[4];
	
	private boolean showAxis = false;
	
	private AtomicBoolean screenshot = new AtomicBoolean(false);
	
	private GLCanvas glCanvas;
	
	private final ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1);

	private Runnable displayTask;

	private ScheduledFuture<?> future;

	private boolean useShaders = false;
	
	private int implementationRenderingProgram;
	
	private int axisRenderingProgram;
	private int[] vertexArray = new int[2];
	private int mvMatrixLoc;
	private int projMatrixLoc;
	private int posLoc;
	private int[] buffers;

	protected abstract void render(GL4 gl);
	protected abstract void startup(GL4 gl);
	protected abstract void shutdown(GL4 gl);
	protected abstract void resize(GL4 gl, int x, int y, int width, int height);
	
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		return null;
	}
	
	protected int getProgram() {
		if(useShaders)
			return implementationRenderingProgram;
		else
			return -1;
	}
	
	protected long getApplicationTime() {
		return System.currentTimeMillis() - initTime;
	}
	
	protected Matrix4f getDefaultProjectionMatrix(ProjectionType type) {
		if (type == ProjectionType.PERSPECTIVE)
			return perspective;
		return null;
	}
	
	protected Matrix4f getLookAtMatrix() {
		return lookAt;
	}
	
	protected int getWidth() {
		return width;
	}
	
	protected int getHeight() {
		return height;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		GLProfile glProfile = GLProfile.get(GLProfile.GL4);
		
		GLCapabilities caps = new GLCapabilities(glProfile);
		caps.setHardwareAccelerated(true);
		caps.setDoubleBuffered(true);
		//caps.setNumSamples(16);
		
		glCanvas = new GLCanvas(parent, SWT.NONE, caps, null);
		glCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		glCanvas.addGLEventListener(this);
		glCanvas.addKeyListener(this);
		glCanvas.addMouseWheelListener(mouseWheelListener);
		
		Composite buttonsContainer = new Composite(parent, SWT.NONE);
		buttonsContainer.setLayout(new GridLayout(4, false));
		buttonsContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnScreenshot = new Button(buttonsContainer, SWT.NONE);
		btnScreenshot.setText("Screenshot");
		btnScreenshot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				
				//TODO use OpenGL synchronization mechanisms
				screenshot.set(true);
			}
		});
		
		fpsLabel = new Label(buttonsContainer, SWT.NONE);
		fpsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
		Button btnDrawAxis = new Button(buttonsContainer, SWT.CHECK | SWT.CENTER);
		btnDrawAxis.setText("Draw axis");
		btnDrawAxis.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				
				showAxis = !showAxis;
			}
		});
		
		/*
		Button toggleBackground = new Button(buttonsContainer, SWT.NONE);
		toggleBackground.setText("Toggle Background");
		toggleBackground.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
			}
		});
		*/
		
		final Button pauseResumeButton = new Button(buttonsContainer, SWT.NONE);
		pauseResumeButton.setText("Pause");
		pauseResumeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				
				if(future != null) {
					future.cancel(false);
					future = null;
					pauseResumeButton.setText("Resume");
				}
				else {
					future = timer.scheduleAtFixedRate(displayTask, 0, 16, TimeUnit.MILLISECONDS);
					pauseResumeButton.setText("Pause");
				}
				pauseResumeButton.getParent().layout();
			}
		});
		
		displayTask = new Runnable() {
			@Override
			public void run() {
				glCanvas.display();
			}
		};
		future = timer.scheduleAtFixedRate(displayTask, 0, 16, TimeUnit.MILLISECONDS);

	}
	
	@Override
	public void dispose() {
		if(future != null) {
			future.cancel(false);
		}
		timer.shutdown();
		super.dispose();
	}

	@Override
	public void setFocus() {
		glCanvas.setFocus();
	}
	
	private void takeScreenshot(GL4 gl) {
		int viewportWidth = width;
		int viewportHeight = height;
		int viewportX = lowerLeftX;
		int viewportY = lowerLeftY;
		
		int screenShotWidth = 1920;
		int screenShotHeight = 1080;
		
		reshape(glCanvas, 0, 0, screenShotWidth, screenShotHeight);
		
		int[] frameBuffer = new int[1];
		gl.glGenFramebuffers(1, frameBuffer, 0);
		gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer[0]);
		
		int[] colorTexture = new int[1];
		gl.glGenTextures(1, colorTexture, 0);
		gl.glBindTexture(GL_TEXTURE_2D, colorTexture[0]);
		gl.glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, screenShotWidth, screenShotHeight);
		
		int[] depthTexture = new int[1];
		gl.glGenTextures(1, depthTexture, 0);
		gl.glBindTexture(GL_TEXTURE_2D, depthTexture[0]);
		gl.glTexStorage2D(GL_TEXTURE_2D, 1, GL_DEPTH_COMPONENT32F, screenShotWidth, screenShotHeight);
		
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, colorTexture[0], 0);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTexture[0], 0);
		
		gl.glDrawBuffers(1, new int[]{GL_COLOR_ATTACHMENT0}, 0);
		
		gl.glViewport(0, 0, screenShotWidth, screenShotHeight);
		
		internalDisplay(gl);
		
		File screenshot = new File("screenshot.bmp");
		File screenshotPng = new File("screenshot.png");
		
		/*
		 * If the screenshot size matches the GLCanvas dimensions, this code would be sufficient
		 * 
		    GLReadBufferUtil util = new GLReadBufferUtil(false, false);
			util.readPixels(gl, false);
			util.write(screenshot);
			util.write(screenshotPng);
		 */
		
		/*
		 * bytesPerPixel: 24
		 * componentCount: 3
		 * format: GL_RGB
		 * type: GL_UNSIGNED_BYTE
		 */
		GLPixelAttributes pixelAttribs = GLPixelBuffer.defaultProviderNoRowStride.getAttributes(gl, /*componentCount*/3);
		
		final int tmp[] = new int[1];
        final int readPixelSize = GLBuffers.sizeof(gl, tmp, pixelAttribs.bytesPerPixel, screenShotWidth, screenShotHeight, 1, true);
        /* readPixelSize = ((screenShotWidth * 3 + 3) & ~3) * screenShotHeight; */

        ByteBuffer buffer = ByteBuffer.allocate(readPixelSize);
		
		TextureData readTextureData = new TextureData(
                gl.getGLProfile(),
                GL_RGB,
                screenShotWidth, screenShotHeight,
                0,
                pixelAttribs,
                false, false,
                false,//flipVertically,
                buffer,
                null /* Flusher */);
		gl.glReadPixels(0, 0, screenShotWidth, screenShotHeight, pixelAttribs.format, pixelAttribs.type, buffer);
		
		try {
			TextureIO.write(readTextureData, screenshot);
			buffer.rewind();
			TextureIO.write(readTextureData, screenshotPng);
		} catch (GLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		gl.glDeleteTextures(1, colorTexture, 0);
		gl.glDeleteTextures(1, depthTexture, 0);
		gl.glDeleteFramebuffers(1, frameBuffer, 0);
		gl.glBindTexture(GL_TEXTURE_2D, 0);
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		reshape(glCanvas, viewportX, viewportY, viewportWidth, viewportHeight);
	}
	
	/** GLEventListener methods **/

	@Override
	public void display(GLAutoDrawable drawable) {
		computeFps();
		
		final GL4 gl = (GL4) drawable.getGL();
		
		//TODO use OpenGL synchronization mechanisms
		if(screenshot.compareAndSet(true, false)) {
			takeScreenshot(gl);
		}
		
		gl.glViewport(lowerLeftX, lowerLeftY, width, height);
		
		long[] startTimestamp = new long[1];
		gl.glGetInteger64v(GL_TIMESTAMP, startTimestamp, 0);
		gl.glQueryCounter(queries[0], GL_TIMESTAMP);
		
		gl.glBeginQuery(GL_TIME_ELAPSED, queries[1]);
		gl.glBeginQuery(GL_PRIMITIVES_GENERATED, queries[2]);
		gl.glBeginQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN, queries[3]);
		
		internalDisplay(gl);
		
		gl.glEndQuery(GL_TIME_ELAPSED);
		gl.glEndQuery(GL_PRIMITIVES_GENERATED);
		gl.glEndQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN);
		
		printQueryResults(gl, startTimestamp[0]);
	}
	
	private void internalDisplay(GL4 gl) {
		gl.glBindVertexArray(vertexArray[0]);
		
		if(useShaders)
			gl.glUseProgram(implementationRenderingProgram);
		
		FloatBuffer black = FloatBuffer.allocate(4);
		black.put(0, 0.0f);
		black.put(1, 0.0f);
		black.put(2, 0.0f);
		black.put(3, 1.0f);
		gl.glClearBufferfv(GL_COLOR, 0, black);
		
		FloatBuffer one = FloatBuffer.allocate(1);
		one.put(0, 1.0f);
		gl.glClearBufferfv(GL_DEPTH, 0, one);
		
		render(gl);
		
		if(showAxis)
			drawAxes(gl);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		GL4 gl = (GL4) drawable.getGL();
		
		if(useShaders) {
			gl.glDeleteProgram(implementationRenderingProgram);
		}
		
		gl.glDeleteVertexArrays(vertexArray.length, vertexArray, 0);
		gl.glDeleteProgram(axisRenderingProgram);
		
		gl.glDeleteBuffers(buffers.length, buffers, 0);
		
		deleteQueries(gl);
		
		shutdown(gl);
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		initTime = System.currentTimeMillis();
		
		initLookAtParameters();
		buildLookAt();
		
		System.out.println(drawable.getGL().getContext().getGLVersion());
		if(!drawable.getGL().isGL4())
			throw new RuntimeException("OpenGL 4 NOT supported on this machine");
		DebugGL4 dbg = new DebugGL4((GL4) drawable.getGL());
		drawable.setGL(dbg);
		
		final GL4 gl4 = (GL4) drawable.getGL();
		
		createShaderPrograms(gl4);
		
		gl4.glGenVertexArrays(vertexArray.length, vertexArray, 0);
		gl4.glBindVertexArray(vertexArray[0]);
		
		startup(gl4);
		
		gl4.glBindVertexArray(vertexArray[1]);
		
		buffers = new int[1];
		gl4.glGenBuffers(buffers.length, buffers, 0);
		gl4.glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
		
		FloatBuffer buffer = FloatBuffer.wrap(AXIS_VERTEX_POSITIONS);
		gl4.glBufferData(GL_ARRAY_BUFFER, buffer.limit() * 4, buffer, GL_STATIC_DRAW);
		
		//	in vec4 position
		posLoc = gl4.glGetAttribLocation(axisRenderingProgram, "position");
		gl4.glVertexAttribPointer(posLoc, 3, GL_FLOAT, false, 0, 0);
		gl4.glEnableVertexAttribArray(posLoc);
		
		/*
			uniform mat4 mv_matrix;
			uniform mat4 proj_matrix;
		 */
		mvMatrixLoc = gl4.glGetUniformLocation(axisRenderingProgram, "mv_matrix");
		projMatrixLoc = gl4.glGetUniformLocation(axisRenderingProgram, "proj_matrix");
		
		gl4.glEnable(GL_DEPTH_TEST);
		gl4.glDepthFunc(GL_LEQUAL);
		
		initQueries(gl4);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		this.width = width;
		this.height = height;
		this.lowerLeftX = x;
		this.lowerLeftY = y;
		
		float aspect = (float) width / (float) height;
		perspective = MatrixUtils.perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		resize((GL4) drawable.getGL(), x, y, width, height);
	}
	
	// Queries
	private void initQueries(GL4 gl) {
		gl.glGenQueries(queries.length, queries, 0);
	}
	
	private void printQueryResults(GL4 gl, long startTimestamp) {
		// 0 - pipeline latency
		// 1 - rendering time
		// 2 - primitive count
		// 3 - transform feedback written primitives
		long[] results = new long[4];
		
		gl.glGetQueryObjectui64v(queries[0], GL_QUERY_RESULT, results, 0);
		gl.glGetQueryObjectui64v(queries[1], GL_QUERY_RESULT, results, 1);
		gl.glGetQueryObjectui64v(queries[2], GL_QUERY_RESULT, results, 2);
		gl.glGetQueryObjectui64v(queries[3], GL_QUERY_RESULT, results, 3);
		
		System.out.println("--------------------------------------------------------------");
		System.out.println("Pipeline latency: " + (results[0] - startTimestamp) + " ns");
		System.out.println("Rendering time: " + results[1] + " ns");
		System.out.println("Primitives generated: " + results[2]);
		System.out.println("Primitives written to transform feedback: " + results[3]);
		System.out.println("--------------------------------------------------------------");
	}
	
	private void deleteQueries(GL4 gl) {
		gl.glDeleteQueries(queries.length, queries, 0);
	}
	
	private void computeFps() {
		if(!started) {
			started = true;
			lastTime = System.currentTimeMillis();
			numFrames = 0;
		}
		else {
			double currentTime = System.currentTimeMillis();
			numFrames++;
			
			double timeDiff = currentTime - lastTime;
			final double mspf = timeDiff / numFrames;
			
			if(timeDiff >= 1000.0) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						if(!fpsLabel.isDisposed()) {
							fpsLabel.setText(String.format("%.3f mspf (%.3f fps)", mspf, 1000.0/mspf));
							fpsLabel.getParent().layout();
						}
					}
				});
				
				numFrames = 0;
				lastTime += timeDiff;
			}
		}
	}
	
	private void drawAxes(GL4 gl) {
		if(axisRenderingProgram != -1) {
			gl.glBindVertexArray(vertexArray[1]);
			gl.glClear(GL_DEPTH_BUFFER_BIT);
			
			gl.glViewport(lowerLeftX, lowerLeftY, width/5, height/5);
			
			gl.glUseProgram(axisRenderingProgram);
			Matrix4f mvMatrix = Matrix4f.multiplyAll(
					axisLookAt, 
					Matrix4f.identity);
			
			gl.glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
			gl.glVertexAttribPointer(posLoc, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(posLoc);
			
			gl.glUniformMatrix4fv(mvMatrixLoc, 1, false, mvMatrix.toArray(), 0);
			gl.glUniformMatrix4fv(projMatrixLoc, 1, false, getDefaultProjectionMatrix(ProjectionType.PERSPECTIVE).toArray(), 0);
			
			gl.glLineWidth(2.0f);
			gl.glDrawArrays(GL_LINES, 0, 6);
		}
	}
	
	private void createShaderPrograms(GL4 gl) {
		cretaImplementationProgram(gl);
		createAxisProgram(gl);
	}
	
	private void createAxisProgram(GL4 gl) {
		System.out.println("Creating base class AXIS rendering program");
		String[] vShaderSource = GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/axisV.glsl"));
		String[] fShaderSource = GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/axisF.glsl"));
		
		int vShader = GLUtils.compileShader(vShaderSource, gl, ShaderType.VERTEX_SHADER);
		
		if(vShader != -1) {
			int fShader = GLUtils.compileShader(fShaderSource, gl, ShaderType.FRAGMENT_SHADER);
			
			if(fShader != -1) {
				axisRenderingProgram = GLUtils.compileProgram(new Integer[]{vShader, fShader}, gl);
				
				gl.glDeleteShader(vShader);
				gl.glDeleteShader(fShader);
			}
			else {
				gl.glDeleteShader(vShader);
			}
		}
	}
	
	private void cretaImplementationProgram(GL4 gl) {
		System.out.println("Creating implementation class rendering program");
		List<Integer> compiledShaders = new ArrayList<Integer>();
		
		for(ShaderType shaderType : ShaderType.values()) {
			String[] shaderSource = getShaderSourceLines(shaderType);
			
			if(shaderSource != null) {
				int shader = GLUtils.compileShader(shaderSource, gl, shaderType);
				
				if(shader != -1)
					//In caso di successo
					compiledShaders.add(shader);
				else {
					//In caso di errore
					for(Integer previousShader : compiledShaders)
						gl.glDeleteShader(previousShader);
					return;
				}
			}
		}
		
		if(compiledShaders.size() > 0) {
			implementationRenderingProgram = GLUtils.compileProgram(compiledShaders.toArray(new Integer[0]), gl);
			
			for(Integer shader : compiledShaders)
				gl.glDeleteShader(shader);
			
			if(implementationRenderingProgram != -1)
				useShaders = true;
		}
	}
	
	//CAMERA UTILITIES
	@Override
	public void keyPressed(KeyEvent e) {
		boolean ctrlPressed = (e.stateMask & SWT.CTRL) != 0;
		
		Vec3f direction = target.subtract(camera);
		float distanceToTarget = direction.length();
		
		switch (e.keyCode) {
		case SWT.ARROW_UP:
			if(ctrlPressed) {
				translateCameraUpDown(true);
			}
			else {
				rotateCameraUp(distanceToTarget);
			}
			buildLookAt();
			break;
		case SWT.ARROW_DOWN:
			if(ctrlPressed) {
				translateCameraUpDown(false);
			}
			else {
				rotateCameraDown(distanceToTarget);
			}
			buildLookAt();
			break;
		case SWT.ARROW_LEFT:
			if(ctrlPressed) {
				translateCameraRightLeft(false);
			}
			else {
				rotateCameraLeft(distanceToTarget);
			}
			buildLookAt();
			break;
		case SWT.ARROW_RIGHT:
			if(ctrlPressed) {
				translateCameraRightLeft(true);
			}
			else {
				rotateCameraRight(distanceToTarget);
			}
			buildLookAt();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	private final MouseWheelListener mouseWheelListener = new MouseWheelListener() {

		@Override
		public void mouseScrolled(MouseEvent e) {
			Vec3f direction = target.subtract(camera);
			float distance = direction.length();
			direction = direction.normalize();
			
			if(e.count > 0) {
				if(distance > 0.2f)
					camera = camera.add(direction.multiply(0.2f));
			}
			else
				camera = camera.add(direction.multiply(-0.2f));
			buildLookAt();
		}
	};
	
	private void translateCameraUpDown(boolean positive) {
		Vec3f[] base = computeCamerBase();
		
		Vec3f upVec = base[2];
		
		if(positive) {
			camera = camera.add(upVec.multiply(0.2f));
			target = target.add(upVec.multiply(0.2f));
		}
		else {
			camera = camera.add(upVec.multiply(-0.2f));
			target = target.add(upVec.multiply(-0.2f));
		}
	}
	
	private void translateCameraRightLeft(boolean positive) {
		Vec3f[] base = computeCamerBase();
		
		Vec3f sideVec = base[1];
		
		if(positive) {
			camera = camera.add(sideVec.multiply(0.2f));
			target = target.add(sideVec.multiply(0.2f));
		}
		else {
			camera = camera.add(sideVec.multiply(-0.2f));
			target = target.add(sideVec.multiply(-0.2f));
		}
	}

	private void rotateCameraUp(float distanceToTarget) {
		lastAngleUpDown += CAMERA_ROTATION_ANGLE_STEP;
		if(lastAngleUpDown >= MAX_UP_DOWN)
			lastAngleUpDown = MAX_UP_DOWN;
		
		Vec3f coordinates = cartesianFromSphere(distanceToTarget, lastAngleUpDown, lastAngleLeftRight);
		
		camera = target.add(coordinates);
	}
	
	private void rotateCameraDown(float distanceToTarget) {
		lastAngleUpDown -= CAMERA_ROTATION_ANGLE_STEP;
		if(lastAngleUpDown <= MIN_UP_DOWN)
			lastAngleUpDown = MIN_UP_DOWN;
		
		Vec3f coordinates = cartesianFromSphere(distanceToTarget, lastAngleUpDown, lastAngleLeftRight);
		
		camera = target.add(coordinates);
	}
	
	private void rotateCameraLeft(float distanceToTarget) {
		lastAngleLeftRight -= CAMERA_ROTATION_ANGLE_STEP;
		
		Vec3f coordinates = cartesianFromSphere(distanceToTarget, lastAngleUpDown, lastAngleLeftRight);
		
		camera = target.add(coordinates);
	}
	
	private void rotateCameraRight(float distanceToTarget) {
		lastAngleLeftRight += CAMERA_ROTATION_ANGLE_STEP;
		
		Vec3f coordinates = cartesianFromSphere(distanceToTarget, lastAngleUpDown, lastAngleLeftRight);
		
		camera = target.add(coordinates);
	}
	
	private Vec3f cartesianFromSphere(float len, float theta, float phi) {
		theta = 90.0f - theta;
		
		float coordZ = (float) (len * Math.sin(Math.toRadians(theta)) * Math.cos(Math.toRadians(phi)));
		float coordX = (float) (len * Math.sin(Math.toRadians(theta)) * Math.sin(Math.toRadians(phi)));
		float coordY = (float) (len * Math.cos(Math.toRadians(theta)));
		
		return new Vec3f(coordX, coordY, coordZ);
	}
	
	private void buildLookAt() {
		Vec3f[] base = computeCamerBase();
		
		Vec3f forward = base[0];
		Vec3f sideVec = base[1];
		Vec3f newUp = base[2];
		
		Vec3f axisTarget = new Vec3f(0, 0, 0);
		axisCamera = axisTarget.subtract(forward.multiply(3.0f));
		
		lookAt = new Matrix4f(
				sideVec.x, sideVec.y, sideVec.z, 0,
				newUp.x, newUp.y, newUp.z, 0,
				-forward.x, -forward.y, -forward.z, 0,
				0, 0, 0, 1);
		
		axisLookAt = lookAt.mult(MatrixUtils.translate(-axisCamera.x, -axisCamera.y, -axisCamera.z));
		
		lookAt = lookAt.mult(MatrixUtils.translate(-camera.x, -camera.y, -camera.z));
	}
	
	private Vec3f[] computeCamerBase() {
		Vec3f forward = target.subtract(camera).normalize();
		
		Vec3f sideVec = forward.cross(up);
		
		if(sideVec.length() == 0) {
			//forward and up are coplanar
			Vec3f tmpUp = new Vec3f(up.x, up.y, up.z - 0.0001f);
			sideVec = forward.cross(tmpUp);
		}
		sideVec = sideVec.normalize();
		
		Vec3f newup = sideVec.cross(forward).normalize();
		
		Vec3f[] ret = new Vec3f[] {forward, sideVec, newup};
		return ret;
	}
	
	private void initLookAtParameters() {
		float distance = 4.0f;
		target = new Vec3f(0, 0, 0);
		lastAngleUpDown = 0;
		lastAngleLeftRight = 0;
		camera = cartesianFromSphere(distance, lastAngleUpDown, lastAngleLeftRight);
		up = new Vec3f(0, 1, 0);
	}
	
	private static final int CAMERA_ROTATION_ANGLE_STEP = 5;
	private static final int MIN_UP_DOWN = -89;
	private static final int MAX_UP_DOWN = 89;
	
	private static final float[] AXIS_VERTEX_POSITIONS = {
		0.0f, 0.0f, 0.0f,
		1.0f, 0.0f, 0.0f,
		
		0.0f, 0.0f, 0.0f,
		0.0f, 1.0f, 0.0f,
		
		0.0f, 0.0f, 0.0f,
		0.0f, 0.0f, 1.0f,
	};

}
