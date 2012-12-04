
class Hair
{
  float z = random(-radio,radio);
  float phi = random(TWO_PI);
  float hairLength = random(1,1.2);
  float theta = aslin(z/radio);

  void draw(float drawLength, int strokeHue) {

    float off = (noise(millis() * 0.0005,sin(phi))-0.5) * 0.3;
    float offb = (noise(millis() * 0.0007,sin(z) * 0.01)-0.5) * 0.3;

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

    float len = hairLength * drawLength;
    float xb = xo * len;
    float yb = yo * len;
    float zb = zo * len;
    
    beginShape(LINES);
    stroke(255);
    vertex(x,y,z);
    stroke(strokeHue,255,255);
    vertex(xb,yb,zb);
    endShape();
    
  }
}
