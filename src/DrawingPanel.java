import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import data.*;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;


class DrawingPanel extends GLJPanel {
    GLU glu;
    GLUquadric quad;
    int hemicubeResolution;

    DrawingPanel(int hemiRes) {
        super(new GLCapabilities((GLProfile.getDefault())));

        hemicubeResolution = hemiRes;

        this.addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable drawable) {

                GL2 gl = drawable.getGL().getGL2();
                gl.glMatrixMode(GL2.GL_PROJECTION);
                glu = new GLU();
                quad = glu.gluNewQuadric();
                glu.gluQuadricDrawStyle(quad, GLU.GLU_LINE);

                gl.glClearColor(0f, 0f, 0f, 0f); //black background
                gl.glShadeModel(GL2.GL_SMOOTH);

                gl.glClearDepth(1.0f); //depth buffer setup
                gl.glEnable(GL.GL_DEPTH_TEST); //enable depth testing
                gl.glDepthFunc(GL.GL_LESS);

                gl.glEnable(GL.GL_CULL_FACE);
                gl.glCullFace(GL.GL_BACK);

                //if first pass calculate world coordinates then form factors
                if (Window.newFile) {
                    for (int i = 0; i < Window.faces.size(); i++) {
                        Window.faces.get(i).calculateWorldCoordinates();
                    }
                    calculateFormFactors(drawable);

                    //set calculation time and show hemicube resolution
                    Window.duration = System.currentTimeMillis() - Window.startTime;
                    Window.durationLabel = new JLabel("Calculation time: " + Window.duration/1000 + " seconds. Total Patches: " + Window.faces.size() + ". Hemicube Resolution: " + hemicubeResolution);
                    Window.southPanel.add(Window.durationLabel, BorderLayout.WEST);
                    Window.southPanel.setVisible(false);
                    Window.southPanel.setVisible(true);

                    Window.newFile = false;
                }
            }

            @Override
            public void dispose(GLAutoDrawable drawable) {

            }

            @Override
            public void display(GLAutoDrawable drawable) {
                if (!Window.faces.isEmpty()) {
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

                            //float[] color = face.getReflectance();
                            float[] color = face.getExitance();

                            gl.glColor3f(color[0], color[1], color[2]);
                            gl.glBegin(GL2.GL_QUADS);
                            for (int v = 0; v < face.getWorld().length; v++) {
                                Vertex vert = face.getWorld()[v];
                                gl.glVertex3f(vert.getX(), vert.getY(), vert.getZ());
                            }
                            gl.glEnd();
                        }
                    }

                }
            }

            @Override
            public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
                //setup "camera"
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

        GL2 gl = drawable.getGL().getGL2();

        gl.glPushMatrix();

        Window.formFactors = new float[Window.faces.size()][Window.faces.size()];

        //calculate form factors for every face towards every other face
        for (int f = 0; f < Window.faces.size(); f++) {
            //array used to store pixel counts from face f to every other face
            int pixelCount[] = new int[Window.faces.size()];
            for (int i = 0; i < pixelCount.length; i++) {
                pixelCount[i] = 0;
            }

            Element face = Window.faces.get(f);
            //calculate centroid of face
            float x = 0;
            float y = 0;
            float z = 0;
            for (int v = 0; v < face.getWorld().length; v++) {
                x += face.getWorld()[v].getX();
                y += face.getWorld()[v].getY();
                z += face.getWorld()[v].getZ();
            }
            x /= face.getWorld().length;
            y /= face.getWorld().length;
            z /= face.getWorld().length;
            float centroid[] = {x, y, z};

            float[] norm = face.getNormal();

            //vertex distances from centroid
            int maxDistanceIndex = 0;
            float temp = 0;
            for (int d = 0; d < 4; d++) {
                //a^2 + b^2 = c^2
                float dist = (float) Math.sqrt(Math.pow(face.getWorld()[d].getX() - centroid[0], 2) + Math.pow(face.getWorld()[d].getY() - centroid[1], 2) + Math.pow(face.getWorld()[d].getZ() - centroid[2], 2));
                if (dist > temp) {
                    maxDistanceIndex = d;
                    temp = dist;
                }
            }

            //d1 and d2 are right angles of each other on plane of face f
            //vertex furthest from centroid
            float[] d1 = {face.getWorld()[maxDistanceIndex].getX(), face.getWorld()[maxDistanceIndex].getY(), face.getWorld()[maxDistanceIndex].getZ()};
            float[] d2 = new float[3];
            //normalise d1
            double magnitude = Math.sqrt((double) (d1[0] * d1[0] + d1[1] * d1[1] + d1[2] * d1[2]));
            if (magnitude != 0) {
                d1[0] /= magnitude;
                d1[1] /= magnitude;
                d1[2] /= magnitude;

                float ox = (norm[1] * d1[2]) - (norm[2] * d1[1]);
                float oy = (norm[2] * d1[0]) - (norm[0] * d1[2]);
                float oz = (norm[0] * d1[1]) - (norm[1] * d1[0]);
                d2[0] = ox;
                d2[1] = oy;
                d2[2] = oz;
            } else {
                d2 = d1;
            }

            //half length of the unit hemicube
            float r = 0.5f;

            float left = 0, right = 0, bottom = 0, top = 0;
            float[] eye = centroid;
            float[] lookat = new float[3], up = new float[3];

            //for each face of hemicube, 5 faces, 1 face doesn't exist/behind "camera"
            for (int i = 0; i < 5; i++) {
                switch (i) { //set "camera" parameters
                    case 0: //front face view
                        left = -r;
                        right = r;
                        bottom = -r;
                        top = r;
                        lookat[0] = centroid[0] + norm[0];
                        lookat[1] = centroid[1] + norm[1];
                        lookat[2] = centroid[2] + norm[2];
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
                        up = norm;
                        break;
                    case 2: //right face view
                        left = -r;
                        right = r;
                        bottom = 0;
                        top = r;
                        lookat[0] = centroid[0] - d1[0];
                        lookat[1] = centroid[1] - d1[1];
                        lookat[2] = centroid[2] - d1[2];
                        up = norm;
                        break;
                    case 3: //bottom face view
                        left = -r;
                        right = r;
                        bottom = 0;
                        top = r;
                        lookat[0] = centroid[0] - d2[0];
                        lookat[1] = centroid[1] - d2[1];
                        lookat[2] = centroid[2] - d2[2];
                        up = norm;
                        break;
                    case 4: //top face view
                        left = -r;
                        right = r;
                        bottom = 0;
                        top = r;
                        lookat[0] = centroid[0] + d2[0];
                        lookat[1] = centroid[1] + d2[1];
                        lookat[2] = centroid[2] + d2[2];
                        up = norm;
                        break;
                }

                //setup viewport
                float viewWidth = (right - left) / (2 * r) * hemicubeResolution;
                float viewHeight = (top - bottom) / (2 * r) * hemicubeResolution;
                gl.glViewport(0, 0, (int) viewWidth, (int) viewHeight);

                //hemicube's side as viewplane
                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glFrustumf(left, right, bottom, top, r, 1000f); //sets clipping planes
                //move camera to face coordinates
                glu.gluLookAt(eye[0], eye[1], eye[2], lookat[0], lookat[1], lookat[2], up[0], up[1], up[2]);
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glLoadIdentity();

                //rasterize entire scene onto this side of hemicube
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                renderColor(drawable);

                ByteBuffer buffer = ByteBuffer.allocateDirect((int) viewWidth * (int) viewHeight * 3); //bytebuffer contains signed bytes in java by default
                gl.glReadPixels(0, 0, (int) viewWidth, (int) viewHeight, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, buffer); //get contents from frame buffer

                //count pixels for this face of hemicube
                for (int h = 0; h < viewHeight; h++) {
                    for (int w = 0; w < viewWidth; w++) {
                        byte bR = buffer.duplicate().get((int) (h * viewWidth + w) * 3);
                        byte bG = buffer.duplicate().get((int) (h * viewWidth + w) * 3 + 1);
                        byte bB = buffer.duplicate().get((int) (h * viewWidth + w) * 3 + 2);

                        float cR = Byte.toUnsignedInt(bR);
                        float cG = Byte.toUnsignedInt(bG);
                        float cB = Byte.toUnsignedInt(bB);
                        float[] color = {cR, cG, cB};

                        int fa = decodeColor(color);
                        //if fa==-1, empty pixel
                        if (fa != -1) {
                            pixelCount[fa]++;
                        }
                    }
                }
            }
            //end for loop, pixels now counted on each side of hemicube for face f

            //x * y * 3 (x * y = front view, 0.5(x*y) for other views, total 3(x*y)
            int totalPixels = 3 * hemicubeResolution * hemicubeResolution;

            //compute form factors for face f
            for (int k = 0; k < Window.faces.size(); k++) {
                if (k == f) {
                    //if both faces are equal, formfactor=0 because no form factor towards itself
                    Window.formFactors[f][k] = 0;
                } else {
                    //set form factor to percentage of pixels of a particular face detected
                    float ff = (float) pixelCount[k] / (float) totalPixels; //need to cast to float first, otherwise ff will be computed as int then casted to float, which always results in 0
                    Window.formFactors[f][k] = ff;
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

            //glColor only accepts values within 0-1, normalise values
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
        int c2 = (int) color[1] << 8;
        int c3 = (int) color[2] << 16;
        return c1 + c2 + c3 - 1;
    }

    public void calculateRadiosities() {
        //calculate radiosities
        //radiosity of surface = reflectivity * incident(form factor) + exitance
        for (int i = 0; i < Window.faces.size(); i++) {
            Window.radiosities.set(i, Window.faces.get(i).getEmission()); //set radiosity to 1,1,1 if light, otherwise 0
            for (int j = 0; j < Window.faces.size(); j++) {

                float r1 = Window.faces.get(i).getReflectance()[0] * Window.formFactors[j][i] * Window.radiosities.get(j)[0];
                float g1 = Window.faces.get(i).getReflectance()[1] * Window.formFactors[j][i] * Window.radiosities.get(j)[1];
                float b1 = Window.faces.get(i).getReflectance()[2] * Window.formFactors[j][i] * Window.radiosities.get(j)[2];

                float r2 = r1 + Window.radiosities.get(i)[0];
                float g2 = g1 + Window.radiosities.get(i)[1];
                float b2 = b1 + Window.radiosities.get(i)[2];

                Window.faces.get(i).setExitance(new float[]{r2 * 50, g2 * 50, b2 * 50});
                Window.radiosities.set(i, new float[]{r2, g2, b2});
            }
        }
    }

}