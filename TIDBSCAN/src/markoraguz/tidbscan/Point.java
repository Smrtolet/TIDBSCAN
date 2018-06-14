package markoraguz.tidbscan;

public class Point {

	private String id;
	private double[] values = null;
	private boolean isCore;
	private boolean isClassed;
	private double distanceToOrigin;

	public boolean isClassed() {
		return isClassed;
	}

	public void setID(String Id) {
		this.id = Id;
	}

	public String getID() {
		return this.id;
	}

	public void setClassed(boolean isClassed) {
		this.isClassed = isClassed;
	}

	public double[] getPointValues() {
		return this.values;
	}

	public int getPointSize() {
		return this.values.length;
	}

	public double getPointValue(int pos) {
		return this.values[pos];
	}

	public void setPointValue(int pos, double value) {
		this.values[pos] = value;
	}

	public boolean isCore() {
		return isCore;
	}

	public void setCore(boolean isCore) {
		this.isCore = isCore;
		this.isClassed = true;
	}

	public double getDistanceToOrigin() {
		return distanceToOrigin;
	}

	public void setDistanceToOrigin(double d) {
		distanceToOrigin = d;
	}

	public Point(double[] values) {
		this.values = values;
	}

	public Point(String str) {

		String[] p = str.split(",");

		setID(p[0]);
		setCore(false);
		setClassed(false);

		this.values = new double[p.length - 1];
		double[] tmpOrigin = new double[p.length - 1]; // 0-point
		for (int i = 1; i < p.length; i++) {
			this.values[i - 1] = Double.parseDouble(p[i]);
			tmpOrigin[i - 1] = 0;
		}

		Point originPoint = new Point(tmpOrigin);
		setDistanceToOrigin(Util.getDistance(this, originPoint));
	}
}
