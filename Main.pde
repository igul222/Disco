import javax.swing.*;
import processing.opengl.*;
import ddf.minim.analysis.*;
import ddf.minim.*;

// float lengthModifierModifier = 1.0; // Mozart
// float lengthModifierModifier = 0.75; // Viva la vida
float lengthModifierModifier = 0.5; // Sandstorm

Minim minim;
AudioPlayer jingle;
FFT fft;

int hairCount = 4000;
hair[] hairs;
float[] z = new float[hairCount]; 
float[] phi = new float[hairCount]; 
float[] lens = new float[hairCount]; 
float radio = 50;
float rx = 0;
float ry = 0;

boolean fileLoaded = false;

float lastLengthModifier = 1.0;
int ballHue = 0;
int iterations = 0;

void draw()
{ 
  if(!fileLoaded && !loadFile()) {
    exit();
    return;
  }
  
  background(0);
  translate(width/2,height/2);
  
  fft.forward(jingle.mix);

  // calculate the average length across the spectrum
  float avg = 0.0;
  int count = 0;
  for(int i=0;i<fft.specSize();i++) {
    count++;
    avg += fft.getBand(i);
  }
  avg /= count;

  // adjust length modifier
  float lengthModifier = lengthModifierModifier * exp(0.4 * avg);
  lengthModifier = constrain(lengthModifier,lastLengthModifier-0.02,lastLengthModifier+0.1);
  lastLengthModifier = lengthModifier;
  
  // calculate hue of hairball:
  
  if(lengthModifier > 2.0) {
    ballHue += 100; // RAVE! RAVE! RAVE!
  } else if(lengthModifier > 1.6) {
    ballHue += 10; // Rave?
  } else if(lengthModifier > 1.3) {
    ballHue += 3; // Smooth color transition
  } else {
    ballHue += 1; // Painfully boring.
  }
  
  // then normalize them
  lengthModifier = constrain(lengthModifier,1.0,3.0);
  ballHue = ballHue % 256;
  
  // rotate hairball
  // most of the work has been done, just scale rotMod based on lenMod
  float rotationModifier = 0.9+lengthModifier*0.05;
  float rxp = ((fakeMouseX()-(width/2))*0.005);
  float ryp = ((fakeMouseY()-(height/2))*0.005);
  rx = (rx*rotationModifier)+(rxp*0.05);
  ry = (ry*rotationModifier)+(ryp*0.05);
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

int fakeMouseX() {
  return (int)((iterations/(frameRate*5)) * width) % width;
}

int fakeMouseY() {
  return ((int)((iterations/(frameRate*5)) * height) + 50) % height;
}

void setup() {
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

boolean loadFile() {
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

void stop()
{
  // always close Minim audio classes when you finish with them  
  if(jingle != null)
    jingle.close();
  
  if(minim != null)
    minim.stop();
  
  super.stop();
}
