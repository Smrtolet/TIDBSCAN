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
	private static String distanceMetric;
	private static Integer M;
	private static String originType;

	public static void main(String[] args) {

		if (args.length == 0) {
			args = new String[3];
			args[0] = "iris.txt";
			args[1] = "0.3";
			args[2] = "5";
			args[3] = "eu"; // distance metric
			args[4] = "0";
		} else {
			if (args.length < 5) {
				System.out.println(
						"Not enough arguments provided. Please add arguments like this: datasetfilename radius minp metric (m) referencePointType\n Where"
								+ " metric can be 'eu' for euclidian, 'mi' for minkowski followed by integer m which is minkowski parameter, and 'ma' for manhattan distance\n"
								+ "referencePointType can be 'min','max','minmax','maxmin', or '0'");
				System.exit(0);
			}
		}

		radius = Double.parseDouble(args[1]);
		minp = Double.parseDouble(args[2]);
		distanceMetric = args[3];
		if (args.length == 5) {
			originType = args[4];
		} else {
			M = Integer.parseInt(args[4]);
			Util.setM(M);
			originType = args[5];
		}

		Util.setMetric(distanceMetric);
		Util.setOriginType(originType);

		long startTime = System.currentTimeMillis();
		try {
			dataSet = Util.getPointsList("datasets/" + args[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		long preprocessingTime = System.currentTimeMillis() - startTime;

		startTime = System.currentTimeMillis();
		applyTIDBSCAN();
		display();

		long execTime = System.currentTimeMillis() - startTime;
		long totalTime = preprocessingTime + execTime;
		
		System.out.println();
		System.out.println("\nPreprocessing time (ms) " + preprocessingTime);
		System.out.println("TIDBSCAN execution time (ms) " + execTime);
		System.out.println("Total time (ms) " + totalTime + "\n");

	}

	private static void display() {
		System.out.println("Distance metric is " + distanceMetric + ", reference point type is " + originType);
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
