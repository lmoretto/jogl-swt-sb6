package jogl.swt.utils;

import static javax.media.opengl.GL4.*;

import java.io.InputStream;
import java.util.Scanner;
import java.util.Vector;

import javax.media.opengl.GL4;
import javax.media.opengl.glu.GLU;

public final class GLUtils {
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
		
		public int getGlShaderTypeId() {
			return glShaderTypeId;
		}
		
		public String getDescription() {
			return description;
		}
		
		private int glShaderTypeId;
		private String description;
	}
	
	public static enum ProjectionType {
		// ORTOGRAPHIC,
		PERSPECTIVE
	}
	
	//SHADER AND PROGRAM UTILITIES
	
	public static int compileProgram(Integer[] shaders, GL4 gl) {
		int[] progLinked = new int[1];
		
		int program = gl.glCreateProgram();
		
		for(Integer shader : shaders)
			gl.glAttachShader(program, shader);
		
		gl.glLinkProgram(program);
		
		checkOpenGLError(gl);
		
		printProgramLog(gl, program);
		gl.glGetProgramiv(program, GL_LINK_STATUS, progLinked, 0);
		if(progLinked[0] == 1) {
			System.out.println("Program linking succeded.");
			
			return program;
		}
		else {
			System.out.println("Program linking failed.");
			gl.glDeleteProgram(program);
			checkOpenGLError(gl);
			return -1;
		}
	}
	
	public static int compileShader(String[] shaderSource, GL4 gl, ShaderType shaderType) {
		int[] shaderCompiled = new int[1];
		
		int[] lengths = new int[shaderSource.length];
		for (int i = 0; i < shaderSource.length; i++) {
			lengths[i] = shaderSource[i].length();
		}
		
		int shader = gl.glCreateShader(shaderType.getGlShaderTypeId());
		gl.glShaderSource(shader, shaderSource.length, shaderSource, lengths, 0);
		gl.glCompileShader(shader);
		
		checkOpenGLError(gl);
		
		printShaderLog(gl, shader);
		gl.glGetShaderiv(shader, GL_COMPILE_STATUS, shaderCompiled, 0);
		if(shaderCompiled[0] == 1) {
			System.out.println(shaderType.getDescription() + " compilation succeded.");
			return shader;
		}
		else {
			System.out.println(shaderType.getDescription() + " compilation failed.");
			gl.glDeleteShader(shader);
			checkOpenGLError(gl);
			return -1;
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
	
	//ERROR UTILITIES
	
	public static boolean checkOpenGLError(GL4 gl) {
		boolean foundError = false;
		
		int glErr = gl.glGetError();
		
		while(glErr != GL_NO_ERROR) {
			System.err.println("glError: " + glu.gluErrorString(glErr));
			foundError = true;
			glErr = gl.glGetError();
		}
		
		return foundError;
	}
	
	public static void printShaderLog(GL4 gl, int shader) {
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
	
	public static void printProgramLog(GL4 gl, int program) {
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
	
	private GLUtils(){}
	
	private static final GLU glu = new GLU();
}
