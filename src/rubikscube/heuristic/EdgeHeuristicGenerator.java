package rubikscube.heuristic;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import rubikscube.RubiksCubeEdges;

public class EdgeHeuristicGenerator {

  private static RubiksCubeEdges cube;
  private static final int MAX_DEPTH = 11;
  private static byte[][] heuristics = {
    new byte[42577920],
    new byte[42577920]
  };

  public static void main(String[] args) {
    for (int i = 0; i < heuristics.length; i++) {
      for (int j = 0; j < heuristics[i].length; j++) {
        heuristics[i][j] = -1;
      }

    }
    System.out.println("Starting to generate file...");
    expand(new byte[0]);
    for (int i = 0; i < heuristics.length; i++) {
      try (PrintWriter writer = new PrintWriter(new FileWriter("heuristic" + (i + 2) + ".txt"))) {
        for (int j = 0; j < heuristics[i].length; j++) {
          writer.println(heuristics[i][j]);
        }
      } catch (IOException e) {
        System.out.println("There is an error writing to the file.");
      }
    }
  }

  public static void expand(byte[] actions) {
    addToTable(actions);
    Queue<byte[]> frontier = new LinkedList<>();
    frontier.add(actions);
    expand(frontier, 0);
  }

  public static void expand(Queue<byte[]> actionset, int depth) {
    if (depth < MAX_DEPTH) {
      Queue<byte[]> frontier = new LinkedList<>();
      for (byte[] actions : actionset) {
        for (byte color : RubiksCubeEdges.getFaces()) {
          for (int i = 0; i < 3; i++) {
            byte[] action = new byte[depth + 1];
            System.arraycopy(actions, 0, action, 0, actions.length);
            action[depth] = encode(color, (byte) (i + 1));
            if (addToTable(action)) {
              frontier.offer(action);
            }
          }
        }
      }
      System.out.println(frontier.size());
      expand(frontier, depth + 1);
    }
  }

  private static boolean addToTable(byte[] actions) {
    boolean added = false;

    cube = new RubiksCubeEdges();

    for (byte action : actions) {
      for (byte face : decode(action)) {
        cube.rotateFace(face);
      }
    }

    int[] states = cube.getStates();
    for (int i = 0; i < states.length; i++) {
      if (heuristics[i][states[i]] == -1) {
        heuristics[i][states[i]] = (byte) actions.length;
        added = true;
      }
    }

    return added;
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
}