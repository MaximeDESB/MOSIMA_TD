package sma.ourActionsBehaviours;

import java.util.ArrayList;

import org.jpl7.Query;

import com.jme3.math.Vector3f;

import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.agents.FinalAgent;

public class OurAttack extends TickerBehaviour{
	
	private static final long serialVersionUID = 4340498260100499547L;
	
	public static long FORGET_TIME = 35;;
	
	FinalAgent agent;
	
	String enemy;
	long lastTimeSeen;
	Vector3f lastPosition;
	
	public static boolean openFire = false;

	public OurAttack(Agent a, long period, String enemy) {
		super(a, period);
		this.enemy = enemy;
		agent = (FinalAgent)((AbstractAgent)a);
		lastPosition = agent.getEnemyLocation(enemy);
		lastTimeSeen = System.currentTimeMillis();
		openFire = false;
		System.out.println("Player Attack");
		onTick();
	}

	

	@Override
	protected void onTick() {
		
		askForFirePermission();
		
		//ici nous ajoutons notre code
		Vector3f lowest = findLowestNeighbor();
		Vector3f highest = findHighestNeighbor();
		Vector3f midest = findMidestNeighbor();
		Vector3f move = null;
		
		double random = Math.random();
		if (random <= 0.5865) {
			move = lowest;
		}else if (random <= 0.8625) {
			move = highest;
		}else {
			move = midest;
		}

		if (move!=null) {
			System.out.println("test");
			agent.goTo(move);
		}else {
			agent.goTo(lastPosition);
		}
	
		
		System.out.println("remi attack");
		
		if(agent.isVisible(enemy, AbstractAgent.VISION_DISTANCE)){
			lastTimeSeen = System.currentTimeMillis();
			lastPosition = agent.getEnemyLocation(enemy);
			agent.lookAt(lastPosition);
			
			if (openFire){
				System.out.println("Enemy visible, FIRE !");
				agent.lastAction = Situation.SHOOT;
				agent.shoot(enemy);
				
			}
			
		}else{
			
			if (System.currentTimeMillis() - lastTimeSeen > FORGET_TIME * getPeriod()){
				System.out.println("The enemy ran away");
				agent.removeBehaviour(this);
				agent.currentBehavior = null;
			}
			agent.lastAction = Situation.FOLLOW;
			
		}
	}
	
	public static void askForFirePermission(){
		String query = "our_toOpenFire("
					+OurPrologBehavior.sit.enemyInSight +","
					+OurPrologBehavior.sit.impactProba+")";
		
		openFire = Query.hasSolution(query);
	}
	
	Vector3f findHighestNeighbor() {
		ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.NEIGHBORHOOD_DISTANCE,
				AbstractAgent.CLOSE_PRECISION, AbstractAgent.VISION_ANGLE);
		return getHighest(points, lastPosition);
	}

	Vector3f findLowestNeighbor() {
		ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.NEIGHBORHOOD_DISTANCE,
				AbstractAgent.CLOSE_PRECISION, AbstractAgent.VISION_ANGLE);
		return getLowest(points, lastPosition);
	}
	
	Vector3f findMidestNeighbor() {
		ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.NEIGHBORHOOD_DISTANCE,
				AbstractAgent.CLOSE_PRECISION, AbstractAgent.VISION_ANGLE);
		return getMidest(points, lastPosition);
	}
	
	Vector3f getHighest(ArrayList<Vector3f> points, Vector3f enemy) {
		float maxHeight = -256;
		Vector3f best = null;

		for (Vector3f v3 : points) {
			if (v3.getY() > maxHeight) {
				best = v3;
				maxHeight = v3.getY();
			}
		}
		if (best.getY() > enemy.getY()+3) {
			return best;
		}else {
			return null;
		}
	}

	Vector3f getLowest(ArrayList<Vector3f> points, Vector3f enemy) {
		float minHeight = 256;
		Vector3f best = null;

		for (Vector3f v3 : points) {
			if (v3.getY() < minHeight) {
				best = v3;
				minHeight = v3.getY();
			}
		}
		if (best.getY() <= enemy.getY()-3) {
			return best;
		}else {
			return null;
		}
	}
	
	Vector3f getMidest(ArrayList<Vector3f> points, Vector3f enemy) {
		float minHeight = 256;
		float maxHeight = -256;
		Vector3f best = null;

		for (Vector3f v3 : points) {
			if (v3.getY() <= enemy.getY()+3 && v3.getY() > enemy.getY()-3) {
				best = v3;
			}
		}
		return best;
	}

}














