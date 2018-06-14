package markoraguz.tidbscan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class TIDBSCAN {
	private static double radius;
	private static double minp;
	private static List<Point> dataSet = new ArrayList<Point>();
	private static List<List<Point>> clusters = new ArrayList<List<Point>>();

	public static void main(String[] args) {

		if (args.length != 3) {
			args = new String[3];
			args[0] = "iris.txt";
			args[1] = "0.3";
			args[2] = "5";
		}

		try {
			dataSet = Util.getPointsList("datasets/" + args[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		radius = Double.parseDouble(args[1]);
		minp = Double.parseDouble(args[2]);

		applyTIDBSCAN();
		display();

	}

	private static void display() {
		System.out.println("Clusters " + clusters.size());

		System.out.println("Attributes " + dataSet.get(0).getPointSize());

		System.out.println("Points " + dataSet.size());

		for (List<Point> cluster : clusters) {

			System.out.println();

			for (Point p : cluster) {

				System.out.print((clusters.indexOf(cluster) + 1) + " " + p.getID() + " ");

				for (double e : p.getPointValues()) {
					System.out.print(e + " ");
				}

				System.out.println();
			}

		}

		System.out.println("\nNoise points");
		int count = 0;
		for (Point a : dataSet)
			if (a.isClassed() == false) {
				count++;
				System.out.print(" " + a.getID() + " ");
				for (double e : a.getPointValues()) {
					System.out.print(e + " ");
				}
				System.out.println();
			}
		if (count == 0)
			System.out.print("None");

	}

	private static void applyTIDBSCAN() {
		List<Point> sortedByOrigin = new ArrayList<Point>();
		sortedByOrigin.addAll(dataSet);

		Collections.sort(sortedByOrigin, new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				if (o1.getDistanceToOrigin() == o2.getDistanceToOrigin())
					return 0;
				return o1.getDistanceToOrigin() < o2.getDistanceToOrigin() ? -1 : 1;
			}
		});

		List<List<Point>> cores = new ArrayList<List<Point>>();

		for (Iterator<Point> it = sortedByOrigin.iterator(); it.hasNext();) {

			Point p = it.next();

			List<Point> core = new ArrayList<Point>();
			core = Util.findCore(sortedByOrigin, p, radius, minp);

			if (core != null) {
				cores.add(core);
			}

		}

		System.out.println("\nCore Points:" + cores.size());

		// join cores

		while (cores.size() > 0) {

			List<Point> coreA = cores.get(0);

			boolean change = false;
			int pos = 1;
			int size = cores.size();

			for (int i = 1; i < size; i++) {

				List<Point> coreB = cores.get(pos);
				if (coreA == coreB)
					continue; // this never happens, but still
				if (Util.mergeCores(coreA, coreB)) {
					change = true;
					cores.remove(coreB);
				} else
					pos++;

			}
			if (change == false) {
				clusters.add(coreA);
				cores.remove(coreA);
			}
		}

	}

}
