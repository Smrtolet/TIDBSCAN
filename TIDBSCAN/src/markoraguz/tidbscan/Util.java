package markoraguz.tidbscan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Util {
	public static String metric;
	public static Point originPoint; // it is a reference point
	public static String originType; // reference point type, can be max, min , minmax, maxmin and 0
	public static int M; // minkowski attribute

	public static void setOriginPoint(Point orgn) {
		originPoint = orgn;
	}

	public static Point getOriginPoint() {
		return originPoint;
	}

	public static void setOriginType(String origin) {
		originType = origin;
	}

	public static String getOriginType() {
		return originType;
	}

	public static void setMetric(String mtr) {
		metric = mtr;
	}

	public static String getMetric() {
		return metric;
	}

	public static double getDistance(Point p, Point q) {

		int sizeP = p.getPointSize();
		double distance = 0.0;
		switch (metric) {
		case "eu":
			double sum = 0;
			for (int i = 0; i < sizeP; i++) {

				double aux = p.getPointValue(i) - q.getPointValue(i);
				sum += aux * aux;
			}
			distance = Math.sqrt(sum);
			break;
		case "mi":
			double suma = 0;
			for (int i = 0; i < sizeP; i++) {
				double aux = Math.abs(p.getPointValue(i) - q.getPointValue(i));
				suma += Math.pow(aux, M);
			}
			distance = Math.pow(suma, 1.0 / M);
			break;
		case "ma":
			double sumar = 0;
			for (int i = 0; i < sizeP; i++) {
				double aux = Math.abs(p.getPointValue(i) - q.getPointValue(i));
				sumar += aux;
			}
			distance = sumar;
			break;
		}

		return distance;
	}

	public static List<Point> getPointsList(String txtPath) throws IOException {

		List<Point> lst = new ArrayList<Point>();

		BufferedReader br = new BufferedReader(new FileReader(txtPath));

		String str = "";

		while ((str = br.readLine()) != null && str != "") {
			lst.add(new Point(str));
		}
		br.close();

		setUpOriginPoint(lst);
		// set up distance to reference point
		for (Point tmpPoint : lst) {
			tmpPoint.setDistanceToOrigin(getDistance(tmpPoint, originPoint));
		}

		return lst;
	}

	private static void setUpOriginPoint(List<Point> lst) {
		Point max = new Point(lst.get(0).getPointValues());
		Point min = new Point(lst.get(0).getPointValues());
		Point minMax = new Point(lst.get(0).getPointValues());
		Point maxMin = new Point(lst.get(0).getPointValues());
		Point zero = new Point(lst.get(0).getPointValues());
		int pointsize = zero.getPointValues().length;
		for (int i = 0; i < pointsize; i++) {
			zero.setPointValue(i, 0.0);
		}
		if (originType.equals("0")) {
			setOriginPoint(zero);
			return;
		}
		for (Point tmpPoint : lst) {
			double[] tmpValues = tmpPoint.getPointValues();
			for (int i = 0; i < pointsize; i++) {
				if (tmpValues[i] > max.getPointValue(i)) {
					max.setPointValue(i, tmpValues[i]); // maximal values for reference point
				}

				if (tmpValues[i] < min.getPointValue(i)) {
					min.setPointValue(i, tmpValues[i]); // minimal values for reference point
				}
			}
		}

		switch (originType) {
		case "min":
			setOriginPoint(min);
			break;
		case "max":
			setOriginPoint(max);
			break;
		case "minmax":
			for (int i = 0; i < pointsize; i++) {
				if (i % 2 == 0) {
					minMax.setPointValue(i, min.getPointValue(i));
				} else {
					minMax.setPointValue(i, max.getPointValue(i));
				}
			}
			setOriginPoint(minMax);
			break;

		case "maxmin":
			for (int i = 0; i < pointsize; i++) {
				if (i % 2 == 0) {
					maxMin.setPointValue(i, max.getPointValue(i));
				} else {
					maxMin.setPointValue(i, min.getPointValue(i));
				}
			}
			setOriginPoint(maxMin);
			break;

		}

	}

	// find core by comparing only to TI possible
	public static List<Point> findCore(List<Point> lst, Point p, double radius, double minp) {

		int index = getIndexInSortedList(p, lst);
		if (index == -1) {
			System.out.println("ERROR index");
			return null;
		}

		List<Point> tmpLst = new ArrayList<Point>();
		int count = 0;
		// go up the list only up to pessimistic
		for (int i = index; i >= 0; i--) {
			Point q = lst.get(i);
			if (q.getDistanceToOrigin() - p.getDistanceToOrigin() > radius) { // we have gone far enough
				break;
			} else {
				// calculate real distance
				if (getDistance(p, q) <= radius) {
					count++;
					if (!tmpLst.contains(q)) {
						tmpLst.add(q);
					}
				}
			}
		}
		// go down the list
		for (int i = index + 1; i < lst.size(); i++) {
			Point q = lst.get(i);
			if (q.getDistanceToOrigin() - p.getDistanceToOrigin() > radius) { // we have gone far enough
				break;
			} else {
				// calculate real distance
				if (getDistance(p, q) <= radius) {
					count++;
					if (!tmpLst.contains(q)) {
						tmpLst.add(q);
					}
				}
			}
		}

		if (count >= minp) {
			p.setCore(true);
			setListClassed(tmpLst);
			return tmpLst;
		}
		return null;
	}

	private static int getIndexInSortedList(Point p, List<Point> lst) {

		for (int i = 0; i < lst.size(); i++) {
			if (lst.get(i).getDistanceToOrigin() == p.getDistanceToOrigin()) {
				return i;
			}
		}
		return -1;
	}

	public static void setListClassed(List<Point> lst) {
		for (Iterator<Point> it = lst.iterator(); it.hasNext();) {
			Point p = it.next();
			if (!p.isClassed()) {
				p.setClassed(true);
			}
		}
	}

	public static boolean mergeCores(List<Point> a, List<Point> b) {

		boolean merge = false;

		for (int index = 0; index < b.size(); index++) {
			if (a.contains(b.get(index))) {
				merge = true;
				break;
			}
		}
		if (merge) {
			for (int index = 0; index < b.size(); index++) {
				if (!a.contains(b.get(index))) {
					a.add(b.get(index));
				}
			}
		}

		return merge;
	}

	public static void setM(Integer m) {
		M = m;

	}

}
