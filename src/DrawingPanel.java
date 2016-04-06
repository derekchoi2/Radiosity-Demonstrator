import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import data.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

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
                gl.glClearColor(0f, 0f, 0f, 0f); //black background
                gl.glShadeModel(GL2.GL_SMOOTH);

                gl.glClearDepth(1.0f); //depth buffer setup
                gl.glEnable(GL.GL_DEPTH_TEST); //enable depth testing
                gl.glDepthFunc(GL.GL_LEQUAL);

                gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

                //if first pass calculate form factors
                if (Window.pass == 0) {
                    calculateFormFactors(drawable);
                    calculateRadiosities();
                }
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


                    //for each face
                    for (int i = 0; i < Window.faces.size(); i++) {

                        Element face = Window.faces.get(i);
                        if (!(face.getComment().equals("frontwall"))) {
                            gl.glMatrixMode(GL2.GL_MODELVIEW);
                            gl.glLoadIdentity();

                            gl.glScalef(face.getScale()[0], face.getScale()[1], face.getScale()[2]);
                            gl.glRotatef(face.getRotation()[0], 1f, 0f, 0f);
                            gl.glRotatef(face.getRotation()[1], 0f, 1f, 0f);
                            gl.glRotatef(face.getRotation()[2], 0f, 0f, 1f);
                            gl.glTranslatef(face.getTranslation()[0], face.getTranslation()[1], face.getTranslation()[2]);

                            float[] color = face.getReflectance();
                            //Window.radiosities.get(i);

                            gl.glColor3f(color[0], color[1], color[2]);
                            gl.glBegin(GL2.GL_QUADS);
                            for (int v = 0; v < face.getVertices().length; v++) {
                                Vertex vert = face.getVertices()[v];
                                gl.glVertex3f(vert.getX(), vert.getY(), vert.getZ());
                            }
                            gl.glEnd();

//                            //draw each vertex's normal
//                            for (int k = 0; k < face.getVertices().length; k++) {
//                                gl.glBegin(GL2.GL_LINES);
//                                Vertex v = face.getVertices()[k];
//                                gl.glVertex3f(v.getX(), v.getY(), v.getZ());
//                                gl.glVertex3f(v.getX() + (face.getNormal()[0] * 10), v.getY() + (face.getNormal()[1] * 10), v.getZ() + (face.getNormal()[2] * 10));
//                                gl.glEnd();
//                            }
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

    public void calculateFormFactors(GLAutoDrawable drawable) {
        int maxFaces = 4096;
        int hemicubeResolution = 128;

        GL2 gl = drawable.getGL().getGL2();
        //form factor calculations

        gl.glPushMatrix();

        Window.formFactors = new float[Window.faces.size()][Window.faces.size()];

        //for every face
        for (int f = 0; f < Window.faces.size(); f++) {
            int pixelCount[] = new int[maxFaces];
            for (int i = 0; i < pixelCount.length; i++) {
                pixelCount[i] = 0;
            }

            Element face = Window.faces.get(f);
            //calculate centroid of face
            float x = 0;
            float y = 0;
            float z = 0;
            for (int v = 0; v < face.getVertices().length; v++) {
                x += face.getVertices()[v].getX();
                y += face.getVertices()[v].getY();
                z += face.getVertices()[v].getZ();
            }
            x = x / face.getVertices().length;
            y = y / face.getVertices().length;
            z = z / face.getVertices().length;
            float centroid[] = {x, y, z};

            float[] n = face.getNormal();

            //vertex distances from centroid
            int maxDistanceIndex = 0;
            float temp = 0;
            for (int d = 0; d < 4; d++) {
                float dist = (float) Math.sqrt(Math.pow(face.getVertices()[d].getX() - centroid[0], 2) + Math.pow(face.getVertices()[d].getY() - centroid[1], 2) + Math.pow(face.getVertices()[d].getZ() - centroid[2], 2));
                if (dist > temp) {
                    maxDistanceIndex = d;
                    temp = dist;
                }
            }
            //vertex furthest from centroid
            float[] d1 = {face.getVertices()[maxDistanceIndex].getX(), face.getVertices()[maxDistanceIndex].getY(), face.getVertices()[maxDistanceIndex].getZ()};

            float ox = (n[1] * d1[2]) - (d1[1] * n[2]);
            float oy = (n[2] * d1[0]) - (d1[2] * n[0]);
            float oz = (n[0] * d1[1]) - (d1[0] * n[1]);
            float[] d2 = {ox, oy, oz};

            //half length of the unit hemicube
            float r = (float) 0.5;

            float left = 0, right = 0, bottom = 0, top = 0;
            float[] eye = centroid;
            float[] lookat = new float[3];
            float[] up = new float[3];

            //for each face of hemicube, 5 faces, 1 face doesn't exist/behind "camera"
            for (int i = 0; i < 5; i++) {
                switch (i) {
                    case 0: //front face view
                        left = -r;
                        right = r;
                        bottom = -r;
                        top = r;
                        lookat[0] = centroid[0] + n[0];
                        lookat[1] = centroid[1] + n[1];
                        lookat[2] = centroid[2] + n[2];
                        up = d2;
                        break;
                    case 1: //left face view
                        left = -r;
                        right = r;
                        bottom = 0;
                        top = r;
                        lookat[0] = centroid[0] + d1[0];
                        lookat[1] = centroid[1] + d1[1];
                        lookat[2] = centroid[2] + d1[2];
                        up = n;
                        break;
                    case 2: //right face view
                        left = -r;
                        right = r;
                        bottom = 0;
                        top = r;
                        lookat[0] = centroid[0] - d1[0];
                        lookat[1] = centroid[1] - d1[1];
                        lookat[2] = centroid[2] - d1[2];
                        up = n;
                        break;
                    case 3: //bottom face view
                        left = -r;
                        right = r;
                        bottom = 0;
                        top = r;
                        lookat[0] = centroid[0] - d2[0];
                        lookat[1] = centroid[1] - d2[1];
                        lookat[2] = centroid[2] - d2[2];
                        up = n;
                        break;
                    case 4: //top face view
                        left = -r;
                        right = r;
                        bottom = 0;
                        top = r;
                        lookat[0] = centroid[0] + d2[0];
                        lookat[1] = centroid[1] + d2[1];
                        lookat[2] = centroid[2] + d2[2];
                        up = n;
                        break;
                }

                //setup viewport
                float viewWidth = (right - left) / (2 * r) * hemicubeResolution;
                float viewHeight = (top - bottom) / (2 * r) * hemicubeResolution;
                gl.glViewport(0, 0, (int) viewWidth, (int) viewHeight);

                //hemicube's side as viewplane
                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glFrustumf(left, right, bottom, top, r, 1000f);
                glu.gluLookAt(eye[0], eye[1], eye[2], lookat[0], lookat[1], lookat[2], up[0], up[1], up[2]);
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glLoadIdentity();

                //rasterize entire scene onto this side of hemicube
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                renderColor(drawable);

                //gl.glReadBuffer(GL.GL_BACK);
                ByteBuffer buffer = ByteBuffer.allocateDirect((int) viewWidth * (int) viewHeight * 3);
                gl.glReadPixels(0, 0, (int) viewWidth, (int) viewHeight, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, buffer);

//                for (int b = 0; b < buffer.capacity(); b++) {
//                    System.out.println(convertToUnsigned(buffer.get(b)));
//                }

                //count pixels for each face
                for (int ycoord = 0; ycoord < viewHeight; ycoord++) {
                    for (int xcoord = 0; xcoord < viewWidth; xcoord++) {
                        byte b1 = buffer.get((int) (ycoord * viewWidth + xcoord) * 3);
                        byte b2 = buffer.get((int) (ycoord * viewWidth + xcoord) * 3 + 1);
                        byte b3 = buffer.get((int) (ycoord * viewWidth + xcoord) * 3 + 2);

                        float c1 = Byte.toUnsignedInt(b1);
                        float c2 = Byte.toUnsignedInt(b2);
                        float c3 = Byte.toUnsignedInt(b3);
                        float[] color = {c1, c2, c3};

                        int fa = decodeColor(color);
                        //if fa==-1, empty pixel
                        if (fa != -1) {
                            pixelCount[fa]++;
                        }
                    }
                }
                //end for loop, pixels now counted on all 5 sides of hemicube
            }

            int totalPixels = 3 * hemicubeResolution * hemicubeResolution;

            //TODO all form factors come out as 0
            //compute form factors
            for (int k = 0; k < Window.faces.size(); k++) {
                if (k == f) {
                    //if both faces are equal, formfactor=0 because no form factor towards itself
                    Window.formFactors[f][k] = 0;
                    continue;
                }
                float ff = pixelCount[k] / totalPixels;
                Window.formFactors[f][k] = ff;

                if (ff != 0){
                    System.out.println(ff);
                }

            }


        }
        gl.glPopMatrix();
    }

    private void renderColor(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        for (int fi = 0; fi < Window.faces.size(); fi++) {
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();

            Element face = Window.faces.get(fi);

            gl.glScalef(face.getScale()[0], face.getScale()[1], face.getScale()[2]);
            gl.glRotatef(face.getRotation()[0], 1f, 0f, 0f);
            gl.glRotatef(face.getRotation()[1], 0f, 1f, 0f);
            gl.glRotatef(face.getRotation()[2], 0f, 0f, 1f);
            gl.glTranslatef(face.getTranslation()[0], face.getTranslation()[1], face.getTranslation()[2]);

            Vertex v1 = face.getVertices()[0];
            Vertex v2 = face.getVertices()[1];
            Vertex v3 = face.getVertices()[2];
            Vertex v4 = face.getVertices()[3];

            //gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

            //glColor only accepts values within 0-1
            int[] colorCode = Window.facesColorCode.get(fi);
            float[] colorF = new float[3];
            colorF[0] = colorCode[0] / 255f;
            colorF[1] = colorCode[1] / 255f;
            colorF[2] = colorCode[2] / 255f;
            gl.glColor3f(colorF[0], colorF[1], colorF[2]);

            gl.glBegin(GL2.GL_QUADS);
            gl.glVertex3f(v1.getX(), v1.getY(), v1.getZ());
            gl.glVertex3f(v2.getX(), v2.getY(), v2.getZ());
            gl.glVertex3f(v3.getX(), v3.getY(), v3.getZ());
            gl.glVertex3f(v4.getX(), v4.getY(), v4.getZ());
            gl.glEnd();

        }
    }

    private int decodeColor(float[] color) {
        int c1 = (int) color[0];
        int c2 = (int) color[1] >> 8;
        int c3 = (int) color[2] >> 16;
        return c1 + c2 + c3 - 1;
    }

    public void calculateRadiosities() {
        //calculate radiosities
        for (int i = 0; i < Window.faces.size(); i++) {
            Window.radiosities.set(i, Window.faces.get(i).getEmission());
            for (int j = 0; j < Window.faces.size(); j++) {
                float a1 = Window.faces.get(i).getReflectance()[0] * Window.formFactors[j][i] * Window.radiosities.get(j)[0];
                float b1 = Window.faces.get(i).getReflectance()[1] * Window.formFactors[j][i] * Window.radiosities.get(j)[1];
                float c1 = Window.faces.get(i).getReflectance()[2] * Window.formFactors[j][i] * Window.radiosities.get(j)[2];

                float a2 = a1 + Window.radiosities.get(i)[0];
                float b2 = b1 + Window.radiosities.get(i)[1];
                float c2 = c1 + Window.radiosities.get(i)[2];

                Window.faces.get(i).setEmission(new float[]{a2, b2, c2});

                Window.radiosities.set(i, new float[]{a2, b2, c2});
            }
        }
    }

}