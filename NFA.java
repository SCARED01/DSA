package de.unistuttgart.dsass2025.ex10.p1;

import java.sql.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements a non-deterministic finite state automaton.
 * The states are enumerated by the natural numbers including 0.
 * Since this is a non-deterministic automaton, we have a set of possible next states given a state
 * and a character.
 * The character '\0' represents an epsilon transition.
 * Therefore, the alphabet must not contain the character '\0'.
 * For each state, there is a HashMap mapping each character to its set of possible next states. A
 * transition that is not allowed (i.e. a state-character pair that doesn't have a next state) can
 * be defined by putting an empty set into the map or by omitting the character entirely from the
 * map.
 * There is only a single initial state, namely state 0, and a single final state, which can be
 * defined.
 * Multiple initial/final states can be simulated using epsilon transitions.
 */
public class NFA {

    Set<Character> alphabet;
    ArrayList<HashMap<Character, Set<Integer>>> transitions;
    int finalState;

    public NFA(Set<Character> alphabet, ArrayList<HashMap<Character, Set<Integer>>> transitions, int finalState) {
        this.alphabet = alphabet;
        this.transitions = transitions;
        this.finalState = finalState;
    }

    public Set<Character> getAlphabet() {
        return this.alphabet;
    }

    public ArrayList<HashMap<Character, Set<Integer>>> getTransitions() {
        return this.transitions;
    }

    public int getFinalState() {
        return this.finalState;
    }

    public int numberOfStates() {
        return this.transitions.size();
    }

    /**
     * This method checks whether this automaton recognizes a given string
     * @param s the string to check
     * @return true, iff. after reading the whole string, the automaton is in a final state
     */
    public boolean match(String s) {
        final int SCAN = -1;
        Deque<Integer> deque = new ArrayDeque<>();
        Set<Integer> visitedStates = new HashSet<>();
        int j = 0; // s[j] is the next character to read
        int state = 0;
        deque.addLast(SCAN);
        while (j <= s.length()) {
            // We store states which we already processed for the current position in the set
            // visitedStates, so we don't process the same state more than once. In particular,
            // this prevents endless looping through cycles of epsilon edges.
            if (!visitedStates.contains(state)) {
                visitedStates.add(state);

                if (state == this.finalState && j == s.length()) {
                    // Reached end of word AND we're in a final state
                    return true;
                }

                if (state == SCAN) {
                    j++;
                    deque.addLast(SCAN);
                    visitedStates.clear();
                } else {
                    // Possible epsilon transitions
                    Set<Integer> nextStates = this.transitions.get(state).getOrDefault('\0', new HashSet<>());
                    for (int nextState : nextStates) {
                        deque.addFirst(nextState); // Adds the state at the beginning of deque
                    }
                    // Possible character transitions
                    if (j < s.length()) {
                        nextStates = this.transitions.get(state).getOrDefault(s.charAt(j), new HashSet<>());
                        deque.addAll(nextStates); // Adds the states at the end of deque
                    }
                }
            }

            // Transition into new state
            state = deque.removeFirst();
        }
        return false;
    }

    /**
     * This is a helper function that takes transition tables (t1 and t2) of two different automata
     * and adds the states and transitions contained in t2 to t1. Hereby, the state numbers from t2
     * are offset by the number of states of t1, such that they don't conflict with existing states
     * in t1. Essentially, this combines two automata into one by "drawing them next to each other"
     * without any cross edges. Edges "between" the two automata can later be added using the other
     * helper function addTransition (see below).
     *
     * Example:
     * - t1 initially contains two states and a transition (0) -> (1)
     * - t2 contains three states and transitions (0) -> (1) -> (2)
     * In this example, this function adds three states (numbered 2, 3, and 4) and the transitions
     * (2) -> (3) -> (4) to t1.
     *
     * @param t1 the transition table to append to. This will be modified.
     * @param t2 the transition table to append. This will *not* be modified.
     */
    public static void addTransitions(ArrayList<HashMap<Character, Set<Integer>>> t1, ArrayList<HashMap<Character, Set<Integer>>> t2) {
        int offset = t1.size();
        for (int i = 0; i < t2.size(); ++i) {
            HashMap<Character, Set<Integer>> t2Map = t2.get(i);
            HashMap<Character, Set<Integer>> t1Map = new HashMap<>();
            for (char character : t2Map.keySet()) {
                Set<Integer> t2States = t2Map.get(character);
                Set<Integer> t1States = new HashSet<>();
                for (int t2State : t2States) {
                    t1States.add(offset + t2State);
                }
                t1Map.put(character, t1States);
            }
            t1.add(t1Map);
        }
    }

    /**
     * This is a helper function that takes a transition table and adds one more transition to it.
     * @param transitions the transition table. This will be modified.
     * @param state       the state where the transition edge starts
     * @param character   the character on the transition edge
     * @param nextState   the state where the transition edge ends, i.e., the next state for the
     *                    (state, character)-pair. As a (state, character)-pair can have multiple
     *                    next states (in a non-deterministic automaton), this function adds
     *                    nextState to the set of possible next states.
     */
    public static void addTransition(ArrayList<HashMap<Character, Set<Integer>>> transitions, int state, char character, int nextState) {
        if (state < 0 || state >= transitions.size() || nextState < 0 || nextState >= transitions.size())
            throw new IllegalArgumentException();

        HashMap<Character, Set<Integer>> map = transitions.get(state);
        if (!map.containsKey(character))
            map.put(character, new HashSet<>());
        map.get(character).add(nextState);
    }

    /**
     * This is a helper function that takes a transition table and adds one more state to it. The
     * added state initially has no (ingoing or outgoing) transitions. That is, an empty HashMap
     * is appended to the transition table, representing a state with no transitions, yet.
     * @param transitions the transition table. This will be modified by adding one state.
     * @return the ID of the added state, i.e., the index of the added state's transition map in
     *         the transition table. This is simply the new size of the transition table minus 1,
     *         because the new state is added at the end of the list.
     */
    public static int addState(ArrayList<HashMap<Character, Set<Integer>>> transitions) {
        int state = transitions.size();
        transitions.add(new HashMap<>());
        return state;
    }

    /**
     * This method takes two NFAs nfa1 and nfa2, and returns a new NFA for the concatenation of
     * nfa1 followed by nfa2. nfa1 and nfa2 shall not be altered to reduce side-effects. i.e., a
     * deep copy of their data structures is required.
     *
     * You might find the methods addState(), addTransition() and addTransitions() helpful.
     * Also consider that the two NFAs might not be defined for the same alphabet, so the alphabets
     * need to be merged.
     * In the lecture the final state of the first automaton is the initial state of the second
     * automaton. However, you might also use an epsilon transition instead.
     *
     * @param nfa1 first NFA
     * @param nfa2 second NFA
     * @return NFA for the concatenation of nfa1 followed by nfa2
     */
    public static NFA concat(NFA nfa1, NFA nfa2) {
        Set<Character> newAlphabet = new HashSet<>();
        ArrayList<HashMap<Character, Set<Integer>>> newTransitions = new ArrayList<>(nfa1.getTransitions());

        newAlphabet.addAll(nfa1.getAlphabet());
        newAlphabet.addAll(nfa2.getAlphabet());
        NFA.addTransitions(newTransitions, nfa2.getTransitions());
        NFA.addTransition(newTransitions, nfa1.getFinalState(), '\0', nfa1.getFinalState()+1);
        NFA newNFA = new NFA(newAlphabet, newTransitions, nfa2.getFinalState());
        return newNFA;

    }

    /**
     * This method takes two NFAs nfa1 and nfa2, and returns a new NFA for the union of the
     * languages of nfa1 and nfa2. nfa1 and nfa2 shall not be altered to reduce side-effects. i.e.,
     * a deep copy of their data structures is required.
     *
     * You might find the methods addState(), addTransition() and addTransitions() helpful.
     * Also consider that the two NFAs might not be defined for the same alphabet, so the alphabets
     * need to be merged.
     *
     * @param nfa1 first NFA
     * @param nfa2 second NFA
     * @return NFA for the union of nfa1 and nfa2
     */
    public static NFA union(NFA nfa1, NFA nfa2) {
        //Unite the Alphabet
        Set<Character> newAlphabet = new HashSet<>();
        newAlphabet.addAll(nfa1.getAlphabet());
        newAlphabet.addAll(nfa2.getAlphabet());

        ArrayList<HashMap<Character, Set<Integer>>> newTransitions = new ArrayList<>(nfa1.getTransitions());

        ArrayList<HashMap<Character, Set<Integer>>> newStartTransitions = new ArrayList<>();
        NFA.addState(newStartTransitions);
        //add transitions together
        NFA.addTransitions(newTransitions, nfa2.getTransitions());
        //add one state at the beginning has to be state number 0 hence offset all other states using addTransitions
        NFA.addTransitions(newStartTransitions, newTransitions);
        NFA.addTransition(newStartTransitions, 0, '\0', 1);
        NFA.addTransition(newStartTransitions, 0, '\0', nfa1.getTransitions().size()+2);// offset 1 or 2 ??

        //add one final state with epsilon transitions coming from all final states
        NFA.addState(newStartTransitions);
        NFA.addTransition(newStartTransitions, nfa1.getFinalState(), '\0', newStartTransitions.size()-1);
        NFA.addTransition(newStartTransitions, nfa2.getFinalState(), '\0', newStartTransitions.size()-1);


        return new NFA(newAlphabet ,newStartTransitions, newStartTransitions.size()-1);
    }

    /**
     * This method takes an NFA and returns a new NFA for the Kleene star of the language. nfa
     * shall not be altered to reduce side-effects. i.e., a deep copy of its data structures is
     * required.
     *
     * You might find the methods addState(), addTransition() and addTransitions() helpful.
     * Also consider that the two NFAs might not be defined for the same alphabet, so the alphabets
     * need to be merged.
     *
     * @param nfa NFA
     * @return NFA for the Kleene star of nfa
     */
    public static NFA repetition(NFA nfa) {
        ArrayList<HashMap<Character, Set<Integer>>> newTransitions = new ArrayList<>(nfa.getTransitions());
        //Why exception because 10=10 in the addTransition check but why? transitions size should be one greater than final state??
        NFA.addTransition(newTransitions, 0, '\0', nfa.getFinalState());
        NFA.addTransition(newTransitions, nfa.getFinalState(), '\0', 0);
        return new NFA(nfa.getAlphabet(), newTransitions, nfa.getFinalState());

    }

    public static void main(String[] args) {
        NFA nfa1 = aba();
        NFA nfa2 = dccd();
        NFA nfa3 = union(nfa1, nfa2);
        NFA nfa4 = repetition(nfa3);

        System.out.println(nfa1.match(""));

        // output the transition table
        for (int i = 0; i < nfa4.getTransitions().size(); i++) {
            System.out.println(i + ": " + nfa4.getTransitions().get(i));
        }
        System.out.println(nfa4.match("dccdbadccd"));
        System.out.println(nfa4.match("dccdadccd"));
    }

    // Example on how to construct an NFA for the regular expression a*ba
    public static NFA aba(){
        Set<Character> a = new HashSet<>();
        a.add('a');
        a.add('b');

        ArrayList<HashMap<Character, Set<Integer>>> t = new ArrayList<>();
        addState(t);
        addState(t);
        addState(t);
        addTransition(t, 0, 'a', 0);
        addTransition(t, 0, 'b', 1);
        addTransition(t, 1, 'a', 2);

        return new NFA(a, t, 2);
    }

    // Example on how to construct an NFA for the regular expression dccd
    public static NFA dccd(){
        Set<Character> a = new HashSet<>();
        a.add('c');
        a.add('d');

        ArrayList<HashMap<Character, Set<Integer>>> t = new ArrayList<>();
        addState(t);
        addState(t);
        addState(t);
        addState(t);
        addState(t);
        addTransition(t, 0, 'd', 1);
        addTransition(t, 1, 'c', 2);
        addTransition(t, 2, 'c', 3);
        addTransition(t, 3, 'd', 4);

        return new NFA(a, t, 4);
    }

}