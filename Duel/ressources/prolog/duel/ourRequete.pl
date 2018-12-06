use_module(library(jpl)).

our_explore_points(OffSize, DefSize):-
	OffSize >= 2 * DefSize. /* true if search for defensive */

our_being_attacked(Time):-
	Time<10.

our_areaCovered(Radius, Size, MapWidth):-
	2 * pi * Radius * Size < 0.6 * MapWidth.

our_inGoodHealth(life):-
	life>3.

our_shotImpact(Probability):-
	Probability>0.1.

/* DECISIONS */

/* Exporation
 * On explore si l'on a pas été attaqué depuis 10 (time) ou plus.
 *
 * */
our_explore(Time,Size,Radius,MapWidth):-
	not(our_being_attacked(Time)),
	our_areaCovered(Radius,Size,MapWidth),
	jpl_call('sma.ourActionsBehaviours.OurPrologBehavior',ourExecuteExplore,[],@(void)).

our_hunt(Life,Time,OffSize,DefSize,Radius,MapWidth,EnemyInSight):-
	not(our_being_attacked(Time)),
	not(our_areaCovered(Radius,OffSize,MapWidth)),
	not(our_areaCovered(Radius,DefSize,MapWidth));
	our_inGoodHealth(Life),
	our_being_attacked(Time),
	not(EnemyInSight),
	jpl_call('sma.ourActionsBehaviours.OurPrologBehavior',ourExecuteHunt,[],@(void)).

our_toOpenFire(EnemyInSight,P):-
	our_shotImpact(P),
	EnemyInSight.

our_attack(EnemyInSight):-
	EnemyInSight,
	jpl_call('sma.ourActionsBehaviours.OurPrologBehavior',ourExecuteAttack,[],@(void)).


/*our_retreat(Life,Time):-  */
/*	not(inGoodHealth(Life)), */
/*	being_attacked(Time),  */
/*	jpl_call('sma.actionsBehaviours.OurPrologBehavior',executeRetreat,[],@(void)). */
