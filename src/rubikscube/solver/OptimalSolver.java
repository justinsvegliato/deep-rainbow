package rubikscube.solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;
import rubikscube.RubiksCube;

public class OptimalSolver {

  private static OptimalSolver instance;
  private static RubiksCube cube;

  public static void main(String[] args) throws FileNotFoundException {
    Scanner scanner = new Scanner(new File("cube.txt"));
    StringBuilder representation = new StringBuilder();
    while (scanner.hasNext()) {
      representation.append(scanner.next());
    }

    OptimalSolver solver = OptimalSolver.getInstance();
    RubiksCube c = new RubiksCube(representation.toString());
    System.out.println(solver.solve(c));
  }
  private static byte[][] heuristics = {
    new byte[88179840],
    new byte[42577920],
    new byte[42577920]
  };

  private OptimalSolver() {
    populateHeuristicTables();
  }

  public static OptimalSolver getInstance() {
    if (instance == null) {
      synchronized (OptimalSolver.class) {
        if (instance == null) {
          instance = new OptimalSolver();
        }
      }
    }
    return instance;
  }

  private static void populateHeuristicTables() {
    for (int i = 0; i < heuristics.length; i++) {
      try (BufferedReader in = new BufferedReader(new FileReader("heuristic" + (i + 1) + ".txt"))) {
        for (int j = 0; j < heuristics[i].length; j++) {
          heuristics[i][j] = Byte.parseByte(in.readLine());
        }
      } catch (IOException e) {
        System.out.println("There is an error reading from the file.");
      }
    }
  }

  public String solve(RubiksCube c) {
    cube = c;
    int[] states = c.getStates();
    Node node = new Node(states, new byte[0], 0, lookupHeuristic(states));
    System.out.println(node);
    System.out.println(cube);

    int depth = node.getHeuristic();
    while (!node.isSolved() && depth < 18) {
      System.out.println("Evaluating with max depth of " + depth);
      PriorityQueue<Node> frontier = new PriorityQueue<>();
      frontier.add(node);
      LinkedList<Node> explored = new LinkedList<>();
      explored.add(node);
      node = solve(frontier, explored, depth++);

    }


    String solution = "";
    for (byte action : node.getActions()) {
      byte[] actions = decode(action);
      solution += RubiksCube.getColor(actions[0]) + "" + actions.length + " ";
    }
    return solution.equals("") ? "Already solved" : solution;
  }

  private Node solve(PriorityQueue<Node> frontier, LinkedList<Node> explored, int depth) {
    Node node = frontier.remove();
    while (!node.isSolved()) {
      Collection<Node> children = expand(node);
      for (Node child : children) {
        if (!explored.contains(child) || child.getCost() < depth) {
          frontier.add(child);
          explored.add(child);
        }
      }

      if (frontier.isEmpty()) {
        return node;
      } else {
        node = frontier.remove();
      }
    }

    return node;
  }

  private Collection<Node> expand(Node node) {
    Collection<Node> nodes = new LinkedList<>();

    RubiksCube c = new RubiksCube(cube);
    for (byte action : node.getActions()) {
      for (byte color : decode(action)) {
        c.rotateFace(color);
      }
    }

    for (byte color : RubiksCube.getFaces()) {
      for (int i = 0; i < 3; i++) {
        c.rotateFace(color);

        int[] states = c.getStates();
        byte[] actions = new byte[node.getActions().length + 1];
        System.arraycopy(node.getActions(), 0, actions, 0, node.getActions().length);
        actions[actions.length - 1] = encode(color, (byte) (i + 1));
        Node n = new Node(states, actions, node.getCost() + 1, lookupHeuristic(states));
        nodes.add(n);
      }
      c.rotateFace(color);
    }

    return nodes;
  }

  private static int lookupHeuristic(int[] states) {
    int heuristic = -1;
    for (int i = 0; i < heuristics.length; i++) {
      heuristic = Math.max(heuristics[i][states[i]], heuristic);
    }
    if (heuristic < 0) {
      heuristic = Integer.MAX_VALUE;
    }
    return heuristic;
  }

  private static byte[] decode(byte action) {
    byte color = (byte) (action / 3);
    int rotations = (action % 3) + 1;

    byte[] actions = new byte[rotations];
    for (int i = 0; i < actions.length; i++) {
      actions[i] = (byte) color;
    }

    return actions;
  }

  private static byte encode(byte color, byte rotations) {
    return (byte) ((color * 3) + (rotations - 1));
  }

  private class Node implements Comparable<Node> {

    private int[] states;
    private byte[] actions;
    private int cost;
    private int heuristic;

    public Node(int[] states, byte[] actions, int cost, int heuristic) {
      this.states = Arrays.copyOf(states, states.length);
      this.actions = Arrays.copyOf(actions, actions.length);
      this.cost = cost;
      this.heuristic = heuristic;
    }

    public int[] getStates() {
      return states;
    }

    public byte[] getActions() {
      return actions;
    }

    public int getCost() {
      return cost;
    }

    public int getHeuristic() {
      return heuristic;
    }

    public boolean isSolved() {
      for (int state : states) {
        if (state > 0) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int compareTo(Node other) {
      return (cost + heuristic) - (other.getCost() + other.getHeuristic());
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof Node && equals((Node) other);
    }

    public boolean equals(Node other) {
      return Arrays.equals(states, other.states);
    }

    @Override
    public String toString() {
      return "Action: " + arrayToString(actions) + " Cost: " + cost + " Heuristic: " + heuristic + " State: " + Arrays.toString(states);
    }

    private String arrayToString(byte[] array) {
      String s = "";

      for (byte b : array) {
        byte[] actions = decode(b);
        s += RubiksCube.getColor(actions[0]) + "" + actions.length;
      }

      return s;
    }
  }
}
