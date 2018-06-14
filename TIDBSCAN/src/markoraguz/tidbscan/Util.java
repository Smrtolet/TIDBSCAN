package markoraguz.tidbscan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Util {
	public static double getDistance(Point p, Point q) {

		int sizeP = p.getPointSize();

		double sum = 0;
		for (int i = 0; i < sizeP; i++) {

			double aux = p.getPointValue(i) - q.getPointValue(i);
			sum += aux * aux;
		}

		double distance = Math.sqrt(sum);

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
		return lst;
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

}
