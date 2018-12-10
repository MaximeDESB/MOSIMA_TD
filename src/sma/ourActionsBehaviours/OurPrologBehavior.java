package sma.ourActionsBehaviours;

import org.jpl7.Query;
import org.lwjgl.Sys;

import com.jme3.math.Vector3f;

import dataStructures.tuple.Tuple2;
import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.actionsBehaviours.Attack;
import sma.actionsBehaviours.ExploreBehavior;
import sma.actionsBehaviours.HuntBehavior;
import sma.agents.OurAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OurPrologBehavior extends TickerBehaviour {

	private static final long serialVersionUID = 5739600674796316846L;

	public static OurAgent agent;
	public static Class nextBehavior;

	public static Situation sit;

	public OurPrologBehavior(Agent a, long period) {
		super(a, period);
		agent = (OurAgent) ((AbstractAgent) a);
	}

	@Override
	protected void onTick() {
		try {
			String prolog = "consult('./ressources/prolog/duel/ourRequete.pl')";

			if (!Query.hasSolution(prolog)) {
				System.out.println("Cannot open file " + prolog);
			} else {
				sit = Situation.getCurrentSituation(agent);
				List<String> behavior = Arrays.asList("explore", "hunt", "attack");
				ArrayList<Object> terms = new ArrayList<Object>();

				for (String b : behavior) {
					terms.clear();
					// Get parameters
					if (b.equals("explore")) {
						terms.add(sit.timeSinceLastShot);
						terms.add(((OurExploreBehavior.prlNextOffend) ? sit.offSize : sit.defSize));
						terms.add(InterestPoint.INFLUENCE_ZONE);
						terms.add(NewEnv.MAX_DISTANCE);
					} else if (b.equals("hunt")) {
						terms.add(sit.life);
						terms.add(sit.timeSinceLastShot);
						terms.add(sit.offSize);
						terms.add(sit.defSize);
						terms.add(InterestPoint.INFLUENCE_ZONE);
						terms.add(NewEnv.MAX_DISTANCE);
						terms.add(sit.enemyInSight);
					} else if (b.equals("attack")) {
						// terms.add(sit.life);
						terms.add(sit.enemyInSight);
						// terms.add(sit.impactProba);
					} else { // RETREAT
						terms.add(sit.life);
						terms.add(sit.timeSinceLastShot);
					}

						String query = prologQuery("our_" + b, terms);
						if (Query.hasSolution(query)) {
							// System.out.println("has solution");
							setNextBehavior();

						}
				}
			}
		} catch (Exception e) {
			System.err.println("Behaviour file for Prolog agent not found");
			System.exit(0);
		}
		randomMove();
	}
	
	void randomMove() {
		long time = System.currentTimeMillis();
		
			agent.randomMove();
			
		
	}

	public void setNextBehavior() {

		if (agent.currentBehavior != null && nextBehavior == agent.currentBehavior.getClass()) {
			return;
		}
		if (agent.currentBehavior != null) {
			agent.removeBehaviour(agent.currentBehavior);
		}

		if (nextBehavior == OurExploreBehavior.class) {
			OurExploreBehavior ex = new OurExploreBehavior(agent, OurAgent.PERIOD);
			agent.addBehaviour(ex);
			agent.currentBehavior = ex;

		} else if (nextBehavior == OurHuntBehavior.class) {
			OurHuntBehavior h = new OurHuntBehavior(agent, OurAgent.PERIOD);
			agent.currentBehavior = h;
			agent.addBehaviour(h);

		} else if (nextBehavior == Attack.class) {

			OurAttack a = new OurAttack(agent, OurAgent.PERIOD, sit.enemy);
			agent.currentBehavior = a;
			agent.addBehaviour(a);

		}

	}

	public String prologQuery(String behavior, ArrayList<Object> terms) {
		String query = behavior + "(";
		for (Object t : terms) {
			query += t + ",";
		}
		return query.substring(0, query.length() - 1) + ")";
	}

	public static void ourExecuteExplore() {
		// System.out.println("explore");
		nextBehavior = OurExploreBehavior.class;
	}

	public static void ourExecuteHunt() {
		// System.out.println("hunt");
		nextBehavior = OurHuntBehavior.class;
	}

	public static void ourExecuteAttack() {
		// System.out.println("attack");
		nextBehavior = OurAttack.class;
	}

	public static void ourExecuteRetreat() {
		// System.out.println("retreat");
		// nextBehavior = RetreatBehavior.class;
	}

}