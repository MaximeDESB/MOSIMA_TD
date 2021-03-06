package sma.ourActionsBehaviours;

import org.jpl7.Query;

import com.jme3.math.Vector3f;

import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.agents.FinalAgent;

public class OurAttack extends TickerBehaviour {

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
		agent = (FinalAgent) ((AbstractAgent) a);
		lastPosition = agent.getEnemyLocation(enemy);
		lastTimeSeen = System.currentTimeMillis();
		openFire = false;
		System.out.println("Our Player Attacks ?");
	}

	@Override
	protected void onTick() {
		System.out.println("Yes, he attacks (our player)");

		askForFirePermission();

		agent.goTo(lastPosition);

		if (agent.isVisible(enemy, AbstractAgent.VISION_DISTANCE)) {
			lastTimeSeen = System.currentTimeMillis();
			lastPosition = agent.getEnemyLocation(enemy);
			agent.lookAt(lastPosition);

			if (openFire) {
				System.out.println("Enemy visible, FIRE !");
				agent.lastAction = Situation.SHOOT;
				agent.shoot(enemy);
			}

		} else {

			if (System.currentTimeMillis() - lastTimeSeen > FORGET_TIME * getPeriod()) {
				System.out.println("The enemy ran away");
				agent.removeBehaviour(this);
				agent.currentBehavior = null;
			}
			agent.lastAction = Situation.FOLLOW;

		}
	}

	public static void askForFirePermission() {
		System.out.println("Remi issuing <openfire?>(insight: " + OurPrologBehavior.sit.enemyInSight + ", impactProba: "
				+ OurPrologBehavior.sit.impactProba + ")");
		
		String query = "toOpenFire(" + OurPrologBehavior.sit.enemyInSight + "," + OurPrologBehavior.sit.impactProba
				+ ")";
		openFire = Query.hasSolution(query);

		System.out.println("Prolog Answer : " + (openFire ? "GRANTED" : "DENIED"));
	}

}
