import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import data.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;


class DrawingPanel extends GLJPanel {
    GLU glu;
    GLUquadric quad;

    DrawingPanel() {
        super(new GLCapabilities((GLProfile.getDefault())));

        this.addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable drawable) {
                GL2 gl = drawable.getGL().getGL2();
                gl.glMatrixMode(GL2.GL_PROJECTION);
                glu = new GLU();
                quad = glu.gluNewQuadric();
                glu.gluQuadricDrawStyle(quad, GLU.GLU_LINE);

                //gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f); //white background
                gl.glClearColor(0f,0f,0f,0f); //black background
                gl.glShadeModel(GL2.GL_SMOOTH);

                gl.glClearDepth(1.0f); //depth buffer setup
                gl.glEnable(GL.GL_DEPTH_TEST); //enable depth testing
                gl.glDepthFunc(GL.GL_LEQUAL);

                gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
            }

            @Override
            public void dispose(GLAutoDrawable drawable) {

            }

            @Override
            public void display(GLAutoDrawable drawable) {
                if (!Window.polyArrayList.isEmpty()) {
                    GL2 gl = drawable.getGL().getGL2();

                    gl.glClear(GL.GL_COLOR_BUFFER_BIT); //clear screen buffer
                    gl.glClear(GL.GL_DEPTH_BUFFER_BIT); //clear depth buffer
                    gl.glEnable(GL.GL_DEPTH_TEST);

                    gl.glLoadIdentity();

                    //for each entity
                    for (int i = 0; i < Window.polyArrayList.size(); i++) {
                        gl.glMatrixMode(GL2.GL_MODELVIEW);
                        gl.glLoadIdentity();

                        Polygon poly = Window.polyArrayList.get(i);
                        //don't render front wall so inside can be shown
                        if (!poly.getComment().equals("frontwall")) {

                            gl.glScalef(poly.getScale()[0], poly.getScale()[1], poly.getScale()[2]);
                            gl.glRotatef(poly.getRotation()[0], 1f, 0f, 0f); //rotate x axis
                            gl.glRotatef(poly.getRotation()[1], 0f, 1f, 0f); //rotate y axis
                            gl.glRotatef(poly.getRotation()[2], 0f, 0f, 1f); //rotate z axis
                            gl.glTranslatef(poly.getTranslation()[0], poly.getTranslation()[1], poly.getTranslation()[2]);

                            //for every surface
                            for (int s = 0; s < poly.getSurfaces().size(); s++) {
                                Surface surface = poly.getSurfaces().get(s);
                                float[] r = surface.getReflectance();
                                float[] ex = surface.getExitance();
                                FloatBuffer reflectance = Buffers.newDirectFloatBuffer(new float[]{r[0], r[1], r[2], 1.0f});
                                FloatBuffer exitance = Buffers.newDirectFloatBuffer(new float[]{ex[0], ex[1], ex[2], 1.0f});

                                //for each patch
                                for (int p = 0; p < surface.getPatches().size(); p++) {
                                    Patch patch = surface.getPatches().get(p);

                                    //for each element
                                    for (int e = 0; e < patch.getElements().size(); e++) {
                                        Element element = patch.getElements().get(e);

                                        //set color of element
                                        float[] eleEmission = element.getEmission();
                                        gl.glColor3f(eleEmission[0], eleEmission[1], eleEmission[2]);

                                        //draw vertices as quad
                                        gl.glBegin(GL2.GL_QUADS);
                                        for (int k = 0; k < element.getVertices().length; k++) {
                                            Vertex v = element.getVertices()[k]; //get each vertex
                                            //plot vertex
                                            gl.glVertex3f(v.getX(), v.getY(), v.getZ());
                                        }
                                        gl.glEnd();
                                    }
                                }
                            }
                        }

                    }

                    gl.glPopMatrix();

                    gl.glFlush();
                }

            }

            @Override
            public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
                GL2 gl = drawable.getGL().getGL2();

                gl.glMatrixMode(GL2.GL_PROJECTION); //select projection matrix
                gl.glLoadIdentity(); //reset Projection Matrix
                gl.glViewport(0, 0, w, h);
                glu.gluPerspective(45.0f, w / h, 0.1f, 100.0f);
                gl.glTranslatef(0, -0.5f, -4);//0,-0.5,-4
                gl.glRotatef(-90f, 1f, 0f, 0f);

                gl.glMatrixMode(GL2.GL_MODELVIEW); //select modelview matrix
                gl.glLoadIdentity(); //reset modelview matrix

            }
        });

    }
}