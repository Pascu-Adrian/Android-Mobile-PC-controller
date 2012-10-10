/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bluetooth;

/**
 *
 * @author PROGRAMARE
 */
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.universe.SimpleUniverse;
import javax.swing.JFrame;

public class Grafica extends JFrame {
Transform3D xtrans = new Transform3D();
Transform3D ytrans = new Transform3D();
Transform3D ztrans = new Transform3D();
Transform3D tmpTrans = new Transform3D();
TransformGroup trgroup = new TransformGroup(xtrans);

  private SimpleUniverse universe;

  private Canvas3D canvas3D;

double currentx=0.0d;
double currenty=0.0d;

   public Grafica(){
   	setSize(500,500);
   	setVisible(true);
   	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   	init();
   }
  public void init() {
    setLayout(new BorderLayout());
    GraphicsConfiguration config = SimpleUniverse
        .getPreferredConfiguration();
    canvas3D = new Canvas3D(config);
    canvas3D.setSize(450,450);
    add("Center", canvas3D);
    BranchGroup brgroup = macheSzene();
    brgroup.compile();
    universe = new SimpleUniverse(canvas3D);
    universe.getViewingPlatform().setNominalViewingTransform();
    universe.addBranchGraph(brgroup);
    //rotatex(0.5d);
  }

  public BranchGroup macheSzene() {
    BranchGroup group = new BranchGroup();
    // Transformation, 2 Rotationen:
    xtrans.mul(ytrans);
    
    trgroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    trgroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    //Loader
    ObjectFile file = new ObjectFile(ObjectFile.RESIZE);
    Scene scene = null;
    try {
        scene = file.load(ClassLoader.getSystemResource("model/ateneav.obj"));

    } catch (Exception e) {
        System.out.println("EROARE:"+e);
    }
    trgroup.addChild(scene.getSceneGroup());

    DirectionalLight d_Licht = new DirectionalLight(new Color3f(1.0f, 0.5f,
        0.3f), new Vector3f(-15.0f, -0.0f, -0.0f));
    d_Licht.setInfluencingBounds(new BoundingSphere(new Point3d(0.0d, 0.0d,
        0.0d), 100.0d));
    trgroup.addChild(d_Licht);

    group.addChild(trgroup);
    return group;
  }
  public void destroy() {
    universe.removeAllLocales();
  }

  public void rotate(double  x,double y){
      tmpTrans=xtrans;
  	AxisAngle4f axax = new AxisAngle4f(0.0f, 0.0f, -1.0f, 0.0f);
  	currentx=currentx+(-5*x);
  	axax.angle = (float) Math.toRadians(currentx);

    tmpTrans.setRotation(axax);
    trgroup.setTransform(xtrans);


    tmpTrans=ytrans;
  	AxisAngle4f axay = new AxisAngle4f(0.0f, -1.0f, 0.0f, 0.0f);
  	currenty=currenty+(-5*y);
  	axay.angle = (float) Math.toRadians(currenty);

    tmpTrans.setRotation(axay);
    xtrans.mul(tmpTrans);
    trgroup.setTransform(xtrans);

  }


}
