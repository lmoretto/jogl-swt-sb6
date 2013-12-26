package jogl.swt.utils.views;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.DebugGL4;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;

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
	private GLCanvas glCanvas;
	
	private final ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1);

	private Runnable displayTask;

	private ScheduledFuture<?> future;
	
	protected abstract void render(GL4 gl);
	protected abstract void startup(GL4 gl);
	protected abstract void shutdown(GL4 gl);

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
		render(gl);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		shutdown((GL4) drawable.getGL());
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		System.out.println(drawable.getGL().getContext().getGLVersion());
		if(!drawable.getGL().isGL4())
			throw new RuntimeException("OpenGL 4 NOT supported on this machine");
		DebugGL4 dbg = new DebugGL4((GL4) drawable.getGL());
		drawable.setGL(dbg);
		
		startup((GL4) drawable.getGL());
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

}
