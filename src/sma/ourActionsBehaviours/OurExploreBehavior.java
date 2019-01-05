package sma.ourActionsBehaviours;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.jpl7.Query;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.InterestPoint.Type;
import sma.actionsBehaviours.LegalActions.LegalAction;
import sma.agents.FinalAgent;
import sma.agents.FinalAgent.MoveMode;
import weka.associations.tertius.IndividualInstance;
import weka.classifiers.trees.J48;
import weka.core.Debug;
import weka.core.Instance;

public class OurExploreBehavior extends TickerBehaviour {

	private static final long serialVersionUID = 4958939169231338495L;

	public static final float RANDOM_MAX_DIST = 10f;
	public static final int RANDOM_REFRESH = 20;

	public static final float VISION_ANGLE = 360f;
	public static final float VISION_DISTANCE = 350f;
	public static final float CAST_PRECISION = 2f;

	public static boolean prlNextOffend;
	public static int probaOffend = 80;

	FinalAgent agent;

	private boolean random_test;
	private Vector3f target;
	private Type targetType;
	

	private long randDate;

	long time;

	public OurExploreBehavior(Agent a, long period) {
		super(a, period);
		agent = (FinalAgent) ((AbstractAgent) a); // I know, i know ...
		target = null;
		randDate = 0;
		time = System.currentTimeMillis();
		prlNextOffend = true;
	}

	protected void onTick() {

		if (target == null && !setTarget() || random_test) {
			randomMove();
			return;
		}
		
		if (agent.getCurrentPosition().distance(target) < AbstractAgent.NEIGHBORHOOD_DISTANCE) {
			Vector3f nei = null;
			try {
				nei = findInterestingNeighbor();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (nei != null && agent.getCurrentPosition().distance(nei) < AbstractAgent.NEIGHBORHOOD_DISTANCE / 2f) {
				// System.out.println("Better Neighbor");
				target = nei;
				agent.moveTo(target);
			} else {
				addInterestPoint();
				target = null;
				// targetType = null;
			}
		}

	}

	void addInterestPoint() {
		if (targetType == Type.Offensive) {
			agent.offPoints.add(new InterestPoint(Type.Offensive, agent));
		} else {
			agent.defPoints.add(new InterestPoint(Type.Defensive, agent));
		}
	}

	Vector3f findInterestingNeighbor() throws Exception {
		ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.NEIGHBORHOOD_DISTANCE,
				AbstractAgent.CLOSE_PRECISION, AbstractAgent.VISION_ANGLE);
		
		points = getRandom(points, 6);
		ArrayList<Vector3f> good = new ArrayList<Vector3f>();
		ArrayList<Vector3f> bad = new ArrayList<Vector3f>();
		
		for (Vector3f point : points) {
			float altitude = point.getY();
			int life = agent.life;
			
	        Instance testInstance = getTestInstance(altitude, life);
	        int result = (int) OurPrologBehavior.dt.classifyInstance(testInstance);
	        if (result == 0) 
	        	good.add(point);
	        else 
	        	bad.add(point);
		}
		
		if (!good.isEmpty()) {
			return getRandom(good, 1).get(0);
		}else {
			return getRandom(bad, 1).get(0);
		}
		
	}
	
	 public Instance getTestInstance (float altitude, float life) {
	        Instance instance = new Instance(3);
	        instance.setDataset(OurPrologBehavior.train);
	        System.out.println(OurPrologBehavior.train.attribute(0));
	        instance.setValue(OurPrologBehavior.train.attribute(0), altitude);
	        instance.setValue(OurPrologBehavior.train.attribute(1), life);
	        instance.setValue(OurPrologBehavior.train.attribute(2), "VICTORY");
	        return instance;
	    }
	
	public ArrayList<Vector3f> getRandom(ArrayList<Vector3f> array, int number) {
	    int rnd = 0;
	    ArrayList<Vector3f> retour = new ArrayList<Vector3f>();
	    for (int i = 0; i<number;i++) {
	    	rnd = new Random().nextInt(array.size());
	    	Vector3f temp = array.get(rnd);
	    	if (!Arrays.asList(retour).contains(temp)) 
	    		retour.add(temp);
	    	else 
	    		i--;
	    }
	    return retour;
	}

	Vector3f findHighestNeighbor() {
		ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.NEIGHBORHOOD_DISTANCE,
				AbstractAgent.CLOSE_PRECISION, AbstractAgent.VISION_ANGLE);
		return getHighest(points);
	}

	Vector3f findLowestNeighbor() {
		ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.NEIGHBORHOOD_DISTANCE,
				AbstractAgent.CLOSE_PRECISION, AbstractAgent.VISION_ANGLE);
		return getLowest(points);
	}

	boolean setTarget() {

		Type t = getNextTargetType();

		if (t == Type.Offensive) {
			target = findOffensiveTarget();
		} else {
			target = findDefensiveTarget();
		}

		if (target != null) {
			agent.goTo(target);
			targetType = t;
			agent.lastAction = (targetType == Type.Offensive) ? Situation.EXPLORE_OFF : Situation.EXPLORE_DEF;
		}

		return target != null;
	}

	Vector3f getHighest(ArrayList<Vector3f> points) {
		float maxHeight = -256;
		Vector3f best = null;

		for (Vector3f v3 : points) {
			if (v3.getY() > maxHeight) {
				best = v3;
				maxHeight = v3.getY();
			}
		}
		return best;
	}

	Vector3f getLowest(ArrayList<Vector3f> points) {
		float minHeight = 256;
		Vector3f best = null;

		for (Vector3f v3 : points) {
			if (v3.getY() < minHeight) {
				best = v3;
				minHeight = v3.getY();
			}
		}
		return best;
	}

	Vector3f findOffensiveTarget() {
		ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.VISION_DISTANCE,
				AbstractAgent.FAR_PRECISION, AbstractAgent.VISION_ANGLE);

		ArrayList<Vector3f> toRemove = new ArrayList<>();

		for (InterestPoint intPoint : agent.offPoints)
			for (Vector3f point : points)
				if (intPoint.isInfluenceZone(point, Type.Offensive))
					toRemove.add(point);

		for (Vector3f v3 : toRemove) {
			points.remove(v3);
		}

		return getHighest(points);
	}

	Vector3f findDefensiveTarget() {
		ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.VISION_DISTANCE,
				AbstractAgent.FAR_PRECISION, AbstractAgent.VISION_ANGLE);

		ArrayList<Vector3f> toRemove = new ArrayList<>();

		for (InterestPoint intPoint : agent.defPoints)
			for (Vector3f point : points)
				if (intPoint.isInfluenceZone(point, Type.Defensive))
					toRemove.add(point);

		for (Vector3f v3 : toRemove) {
			points.remove(v3);
		}

		return getLowest(points);
	}

	Type getNextTargetType() {

		if (agent.useOurProlog) {


			
			Random r = new Random();
			// theoretically, the function should check in the environment that the
			// conditions for the fish to be hooked are met.
			int x = r.nextInt(100);
			System.out.println(" plus haut ? x = " + x);
			if (x < probaOffend) {
				random_test = false;
				return Type.Offensive;
			} else {
				random_test = true;
				return Type.Offensive;
			}
		}
		return null;
	}

	void randomMove() {
		long time = System.currentTimeMillis();
		if (time - randDate > RANDOM_REFRESH * getPeriod()) {
			agent.randomMove(); // Should be something in the neighborhound of the agent, and not some random
								// point in the all map
			randDate = time;
			// agent.getEnvironement().drawDebug(agent.getCurrentPosition(),
			// agent.getDestination());
		}
	}
}
