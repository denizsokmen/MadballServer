package Server;

public class Vector {
	public float x;
	public float y;
	public float z;

	public Vector(float X, float Y, float Z) {
		x = X;
		y = Y;
		z = Z;
	}

	public static float dist(Vector v1, Vector v2) {
		float dist = (float) Math.sqrt((v2.x - v1.x) * (v2.x - v1.x)
				+ (v2.y - v1.y) * (v2.y - v1.y));

		return dist;
	}

	public Vector() {
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
	}



	public void set(Vector vec) {
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.y;
	}

	public float magnitude() {
		float mag = (float) Math.sqrt(x * x + y * y + z * z);
		return mag;
	}

	public void normalize() {
		div(magnitude());
	}

	public void add(Vector vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
	}

	public void div(float n) {
		this.x /= n;
		this.y /= n;
		this.z /= n;
	}

	public float dot(Vector vec) {
		float dt = this.x * vec.x + this.y * vec.y + this.z * vec.z;

		return dt;
	}

	public void sub(Vector vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
	}

	public Vector inv() {
		Vector v = new Vector();
		v.set(this);
		v.x *= -1;
		v.y *= -1;
		v.z *= -1;
		return v;
	}
}
