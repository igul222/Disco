import processing.core.*; 
import processing.xml.*; 

import javax.swing.*; 
import processing.opengl.*; 
import ddf.minim.analysis.*; 
import ddf.minim.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class AudioVis extends PApplet {






// float lengthModifierModifier = 1.0; // Mozart
// float lengthModifierModifier = 0.75; // Viva la vida
float lengthModifierModifier = 0.5f; // Sandstorm

Minim minim;
AudioPlayer jingle;
FFT fft;

int hairCount = 16000;
hair[] hairs;
float[] z = new float[hairCount]; 
float[] phi = new float[hairCount]; 
float[] lens = new float[hairCount]; 
float radio = 50;
float rx = 0;
float ry = 0;

boolean fileLoaded = false;

float lastLengthModifier = 1.0f;
int ballHue = 0;
int iterations = 0;

public void draw()
{ 
  if(!fileLoaded && !loadFile()) {
    exit();
    return;
  }
  
  background(0);
  translate(width/2,height/2);
  
  fft.forward(jingle.mix);

  // calculate the average length across the spectrum
  float avg = 0.0f;
  int count = 0;
  for(int i=0;i<fft.specSize();i++) {
    count++;
    avg += fft.getBand(i);
  }
  avg /= count;

  // adjust length modifier
  float lengthModifier = lengthModifierModifier * exp(0.4f * avg);
  lengthModifier = constrain(lengthModifier,lastLengthModifier-0.02f,lastLengthModifier+0.1f);
  lastLengthModifier = lengthModifier;
  
  // calculate hue of hairball:
  
  if(lengthModifier > 2.0f) {
    ballHue += 100; // RAVE! RAVE! RAVE!
  } else if(lengthModifier > 1.6f) {
    ballHue += 10; // Rave?
  } else if(lengthModifier > 1.3f) {
    ballHue += 3; // Smooth color transition
  } else {
    ballHue += 1; // Painfully boring.
  }
  
  // then normalize them
  lengthModifier = constrain(lengthModifier,1.0f,3.0f);
  ballHue = ballHue % 256;
  
  // rotate hairball
  // most of the work has been done, just scale rotMod based on lenMod
  float rotationModifier = 0.9f+lengthModifier*0.05f;
  float rxp = ((fakeMouseX()-(width/2))*0.005f);
  float ryp = ((fakeMouseY()-(height/2))*0.005f);
  rx = (rx*rotationModifier)+(rxp*0.05f);
  ry = (ry*rotationModifier)+(ryp*0.05f);
  rotateY(rx);
  rotateX(ry);
  fill(255);
  noStroke();
  sphere(radio);
  
  // draw!
  for (int i=0;i<hairCount;i++){
    hairs[i].draw(lengthModifier,ballHue,255);
  }
    
  iterations++;
  
  // reporting
  if(iterations%15 == 0) {
    println("Mouse at ("+fakeMouseX()+", "+fakeMouseY()+"), "+(int)frameRate+" FPS, "+iterations+" iterations");
  }
}

public int fakeMouseX() {
  return (int)((iterations/(frameRate*5)) * width) % width;
}

public int fakeMouseY() {
  return ((int)((iterations/(frameRate*5)) * height) + 50) % height;
}

public void setup() {
  frameRate(120);
  size(500, 500, OPENGL);
  colorMode(HSB);
    
  // setup for the giant ball of hair
  
  radio = height/5;
  hairs = new hair[hairCount];
  for (int i=0; i<hairCount; i++) {
    hairs[i] = new hair();
  }
  noiseDetail(3);
}

public boolean loadFile() {
  String filename;
  
  JFileChooser fc = new JFileChooser();
  int returnVal = fc.showOpenDialog(this);
  
  if (returnVal == JFileChooser.APPROVE_OPTION) {
    File file = fc.getSelectedFile();
    filename = file.getPath();
  }
  else {
    println("Fatal: no file selected");
    exit();
    return false;
  }
  
  minim = new Minim(this);
  
  jingle = minim.loadFile(filename, 2048);
  jingle.loop();
  
  // create an FFT object that has a time-domain buffer the same size as jingle's sample buffer
  // note that this needs to be a power of two and that it means the size of the spectrum
  // will be 512. see the online tutorial for more info.
  
  fft = new FFT(jingle.bufferSize(), jingle.sampleRate());
  fft.window(FFT.HAMMING);
  
  fileLoaded = true;
  return true;
}

public void stop()
{
  // always close Minim audio classes when you finish with them  
  if(jingle != null)
    jingle.close();
  
  if(minim != null)
    minim.stop();
  
  super.stop();
}


class hair
{
  float z = random(-radio,radio);
  float phi = random(TWO_PI);
  float longness = random(1,1.2f);
  float theta = asin(z/radio);

  public void draw(float lengthModifier, int strokeHue, int strokeSaturation) {

    float off = (noise(millis() * 0.0005f,sin(phi))-0.5f) * 0.3f;
    float offb = (noise(millis() * 0.0007f,sin(z) * 0.01f)-0.5f) * 0.3f;

    float thetaff = theta+off;
    float phff = phi+offb;
    float x = radio * cos(theta) * cos(phi);
    float y = radio * cos(theta) * sin(phi);
    float z = radio * sin(theta);
    float msx= screenX(x,y,z);
    float msy= screenY(x,y,z);

    float xo = radio * cos(thetaff) * cos(phff);
    float yo = radio * cos(thetaff) * sin(phff);
    float zo = radio * sin(thetaff);

    float len = longness * lengthModifier;
    float xb = xo * len;
    float yb = yo * len;
    float zb = zo * len;
    
    beginShape(LINES);
    stroke(255);
    vertex(x,y,z);
    stroke(strokeHue,strokeSaturation,255);
    vertex(xb,yb,zb);
    endShape();
  }
}

  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "AudioVis" });
  }
}
