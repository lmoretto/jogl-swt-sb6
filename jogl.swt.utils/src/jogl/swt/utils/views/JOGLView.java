package jogl.swt.utils.views;

import static javax.media.opengl.GL4.*;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.DebugGL4;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.jogamp.opengl.swt.GLCanvas;

public abstract class JOGLView extends ViewPart implements GLEventListener{
	public static enum ShaderType {
		VERTEX_SHADER(GL_VERTEX_SHADER, "Vertex shader"),
		FRAGMENT_SHADER(GL_FRAGMENT_SHADER, "Fragment shader"),
		TESS_CONTROL_SHADER(GL_TESS_CONTROL_SHADER, "Tesselation control shader"),
		TESS_EVALUATION_SHADER(GL_TESS_EVALUATION_SHADER, "Tesselation evaluation shader"),
		GEOMETRY_SHADER(GL_GEOMETRY_SHADER, "Geometry shader");
		
		private ShaderType(int glShaderTypeId, String description) {
			this.glShaderTypeId = glShaderTypeId;
			this.description = description;
		}
		
		private int getGlShaderTypeId() {
			return glShaderTypeId;
		}
		
		private String getDescription() {
			return description;
		}
		
		private int glShaderTypeId;
		private String description;
	}
	
	
	private GLCanvas glCanvas;
	
	private final ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1);

	private Runnable displayTask;

	private ScheduledFuture<?> future;

	private int renderingProgram;

	private boolean useShaders = false;
	
	private GLU glu = new GLU();
	
	protected abstract void render(GL4 gl);
	protected abstract void startup(GL4 gl);
	protected abstract void shutdown(GL4 gl);
	
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		return null;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		GLProfile glProfile = GLProfile.get(GLProfile.GL4);
		
		GLCapabilities caps = new GLCapabilities(glProfile);
		//caps.setHardwareAccelerated(true);
		caps.setDoubleBuffered(true);
		//caps.setNumSamples(16);
		
		glCanvas = new GLCanvas(parent, SWT.NONE, caps, null);
		glCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		glCanvas.addGLEventListener(this);
		
		Composite buttonsContainer = new Composite(parent, SWT.NONE);
		buttonsContainer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		RowLayout rl_buttonsContainer = new RowLayout(SWT.HORIZONTAL);
		buttonsContainer.setLayout(rl_buttonsContainer);
		
		Button toggleBackground = new Button(buttonsContainer, SWT.NONE);
		toggleBackground.setText("Toggle Background");
		toggleBackground.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
			}
		});
		
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
	
	/** GLEventListener methods **/

	@Override
	public void display(GLAutoDrawable drawable) {
		final GL4 gl = (GL4) drawable.getGL();
		
		if(useShaders)
			gl.glUseProgram(renderingProgram);
		
		FloatBuffer black = FloatBuffer.allocate(4);
		black.put(0, 0.0f);
		black.put(1, 0.0f);
		black.put(2, 0.0f);
		black.put(3, 1.0f);
		gl.glClearBufferfv(GL_COLOR, 0, black);
		
		render(gl);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		GL4 gl = (GL4) drawable.getGL();
		
		if(useShaders)
			gl.glDeleteProgram(renderingProgram);
		
		shutdown(gl);
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		System.out.println(drawable.getGL().getContext().getGLVersion());
		if(!drawable.getGL().isGL4())
			throw new RuntimeException("OpenGL 4 NOT supported on this machine");
		DebugGL4 dbg = new DebugGL4((GL4) drawable.getGL());
		drawable.setGL(dbg);
		
		final GL4 gl4 = (GL4) drawable.getGL();
		
		createShaderPrograms(gl4);
		
		startup(gl4);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}
	
	private void createShaderPrograms(GL4 gl) {
		List<Integer> compiledShaders = new ArrayList<Integer>();
		
		for(ShaderType shaderType : ShaderType.values()) {
			String[] shaderSource = getShaderSourceLines(shaderType);
			
			if(shaderSource != null) {
				int[] shaderCompiled = new int[1];
				
				int[] lengths = new int[shaderSource.length];
				for (int i = 0; i < shaderSource.length; i++) {
					lengths[i] = shaderSource[i].length();
				}
				
				int shader = gl.glCreateShader(shaderType.getGlShaderTypeId());
				gl.glShaderSource(shader, shaderSource.length, shaderSource, lengths, 0);
				gl.glCompileShader(shader);
				
				checkOpenGLError(gl);
				gl.glGetShaderiv(shader, GL_COMPILE_STATUS, shaderCompiled, 0);
				if(shaderCompiled[0] == 1) {
					System.out.println(shaderType.getDescription() + " compilation succeded.");
					compiledShaders.add(shader);
				}
				else {
					System.out.println(shaderType.getDescription() + " compilation failed.");
					printShaderLog(gl, shader);
					
					for(Integer previousShader : compiledShaders)
						gl.glDeleteShader(previousShader);
					
					gl.glDeleteShader(shader);
					checkOpenGLError(gl);
					return;
				}
			}
		}
		
		if(compiledShaders.size() > 0) {
			int[] progLinked = new int[1];
						
			renderingProgram = gl.glCreateProgram();
			
			for(Integer shader : compiledShaders)
				gl.glAttachShader(renderingProgram, shader);
			
			gl.glLinkProgram(renderingProgram);
			
			for(Integer shader : compiledShaders)
				gl.glDeleteShader(shader);
			
			checkOpenGLError(gl);
			gl.glGetProgramiv(renderingProgram, GL_LINK_STATUS, progLinked, 0);
			if(progLinked[0] == 1) {
				System.out.println("Program linking succeded.");
			}
			else {
				System.out.println("Program linking failed.");
				printProgramLog(gl, renderingProgram);
				gl.glDeleteProgram(renderingProgram);
				checkOpenGLError(gl);
				return;
			}
			
			useShaders = true;
		}
	}
	
	public static String[] readShaderSource(InputStream is) {
		String[] ret = null;
		
		if(is != null) {
			Vector<String> lines = new Vector<String>();
			Scanner sc;
			sc = new Scanner(is);
			
			while(sc.hasNext()) {
				lines.addElement(sc.nextLine());
			}
			
			sc.close();
			
			ret = new String[lines.size()];
			
			for(int i = 0; i < lines.size(); i++) {
				ret[i] = lines.elementAt(i) + "\n";
			}
		}
		
		return ret;
	}
	
	//Error utilities
	public boolean checkOpenGLError(GL4 gl) {
		boolean foundError = false;
		
		int glErr = gl.glGetError();
		
		while(glErr != GL_NO_ERROR) {
			System.err.println("glError: " + glu.gluErrorString(glErr));
			foundError = true;
			glErr = gl.glGetError();
		}
		
		return foundError;
	}
	
	public void printShaderLog(GL4 gl, int shader) {
		int[] len = new int[1];
		int[] charsWritten = new int[1];
		byte[] log = null;
		
		//Get the length of the shader compilation log
		gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
		
		if(len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetShaderInfoLog(shader, len[0], charsWritten, 0, log, 0);
			System.out.println("Shader Info Log:");
			for(int i = 0; i < log.length; i++) {
				System.out.print((char) log[i]);
			}
		}
	}
	
	public void printProgramLog(GL4 gl, int program) {
		int[] len = new int[1];
		int[] charsWritten = new int[1];
		byte[] log = null;
		
		//Get the length of the program linking log
		gl.glGetProgramiv(program, GL_INFO_LOG_LENGTH, len, 0);
		
		if(len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetProgramInfoLog(program, len[0], charsWritten, 0, log, 0);
			System.out.println("Program Info Log:");
			for(int i = 0; i < log.length; i++) {
				System.out.print((char) log[i]);
			}
		}
	}

}
