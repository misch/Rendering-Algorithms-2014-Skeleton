package rt;

import javax.vecmath.Tuple3f;

/**
 * Stores a spectrum of color values. In this implementation, we work with RGB colors.
 */
public class Spectrum {

	public float r, g, b;
	
	public Spectrum()
	{
		r = 0.f;
		g = 0.f;
		b = 0.f;
	}
	
	public Spectrum(float r, float g, float b)
	{
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public Spectrum(float gray){
		this(gray,gray,gray);
	}
	
	public Spectrum(Spectrum s)
	{
		this(s.r,s.g,s.b);
	}
	
	public Spectrum(Tuple3f t){
		this(t.x,t.y,t.z);
	}
	
	public void mult(float t)
	{
		r = r*t;
		g = g*t;
		b = b*t;
	}
	
	public void mult(Spectrum s)
	{
		r = r*s.r;
		g = g*s.g;
		b = b*s.b;
	}
	
	public void add(Spectrum s)
	{
		r = r+s.r;
		g = g+s.g;
		b = b+s.b;
	}
	
	public void add(float gray){
		this.add(new Spectrum(gray,gray,gray));
	}
	
	public void sub(Spectrum s){
		r = r-s.r;
		g = g-s.g;
		b = b-s.b;
	}
	
	public void sub(float gray){
		this.sub(new Spectrum(gray,gray,gray));
	}
	
	public void divide(Spectrum s){
		r = r/s.r;
		g = g/s.g;
		b = b/s.b;
	}
	
	public void divide(float gray){
		this.divide(new Spectrum(gray,gray,gray));
	}
	
	
	public void clamp(float min, float max)
	{
		r = Math.min(max,  r);
		r = Math.max(min, r);
		g = Math.min(max,  g);
		g = Math.max(min, g);
		b = Math.min(max,  b);
		b = Math.max(min, b);
	}
	
	public String toString(){
		return "(" + this.r + ", " + this.g + ", " + this.b + ")";
	}
	
	public void sqr(){
		this.r = 	r*r;
		this.g=		g*g;
		this.b =	b*b;
	}
}
