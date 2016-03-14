package rubikscube.heuristic;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import rubikscube.RubiksCubeCorners;

public class CornerHeuristicGenerator {

  private static final int MAX_DEPTH = 1;
  private static byte[] heuristic = new byte[88179840];
  private static RubiksCubeCorners cube;

  public static void main(String[] args) {
    for (int i = 0; i < heuristic.length; i++) {
      heuristic[i] = -1;
    }

    byte[] actions = new byte[0];
    expand(actions);
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(new FileWriter("heuristic1.txt"));  
      for (int i = 0; i < heuristic.length; i++) {
        writer.println(heuristic[i]);
      }
    } catch (IOException e) {
      System.out.println("There is an error writing to the file.");
    } finally {
        writer.close();
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
        for (byte color : RubiksCubeCorners.getFaces()) {
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

      expand(frontier, depth + 1);
    }
  }

  private static boolean addToTable(byte[] actions) {
    boolean added = false;

    cube = new RubiksCubeCorners();
    for (byte action : actions) {
      for (byte face : decode(action)) {
        cube.rotateFace(face);
      }
    }

    int state = cube.getState();
    if (heuristic[state] == -1) {
      heuristic[state] = (byte) actions.length;
      added = true;
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
