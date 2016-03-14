package rubikscube;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RubiksCubeEdges {

  public static final byte YELLOW = 0;
  public static final byte WHITE = 1;
  public static final byte GREEN = 2;
  public static final byte BLUE = 3;
  public static final byte RED = 4;
  public static final byte ORANGE = 5;
  private static final byte[] faces = {YELLOW, WHITE, GREEN, BLUE, RED, ORANGE};
  private static final byte[][] sequences = {
    {RED, BLUE, ORANGE, GREEN},
    {RED, GREEN, ORANGE, BLUE},
    {RED, YELLOW, ORANGE, WHITE},
    {RED, WHITE, ORANGE, YELLOW},
    {WHITE, BLUE, YELLOW, GREEN},
    {YELLOW, BLUE, WHITE, GREEN}
  };
  private static final byte[][] cubies = {
    {YELLOW, RED},
    {YELLOW, BLUE},
    {YELLOW, ORANGE},
    {YELLOW, GREEN},
    {GREEN, RED},
    {GREEN, ORANGE},
    {WHITE, RED},
    {WHITE, GREEN},
    {WHITE, ORANGE},
    {WHITE, BLUE},
    {BLUE, RED},
    {BLUE, ORANGE}
  };
  private static final int num_edges = 12;
  private final byte[][][] edges;
  private static LinkedList<byte[]> defaultEdgePositions1;
  private static LinkedList<byte[]> defaultEdgePositions2;
  
  public static final Map<String, Integer> map = new HashMap<String, Integer>() {{
      put("ygr", 0);
      
  }};

  public RubiksCubeEdges() {
    edges = new byte[2][num_edges / 2][2];
    for (int i = 0; i < edges.length; i++) {
      for (int j = 0; j < edges[i].length; j++) {
        edges[i][j] = Arrays.copyOf(cubies[i * (num_edges / 2) + j], 2);
      }
    }
    
    map.get("ygr");
  }

  public RubiksCubeEdges(RubiksCubeEdges cube) {
    edges = new byte[2][num_edges / 2][2];
    for (int i = 0; i < edges.length; i++) {
      for (int j = 0; j < edges[i].length; j++) {
        edges[i][j] = Arrays.copyOf(cube.edges[i][j], cube.edges[i][j].length);
      }
    }
  }

  public void rotateFace(byte face) {
    for (int i = 0; i < edges.length; i++) {
      for (byte[] position : edges[i]) {
        if (hasFace(position, face)) {
          rotateAroundFace(position, face);
        }
      }
    }
  }

  public void rotateFace(byte color, int rotations) {
    for (int i = 0; i < rotations; i++) {
      rotateFace(color);
    }
  }

  public static byte[] getFaces() {
    return faces;
  }

  @Override
  public String toString() {
    String s = "[";
    int i = 0;
    for (byte[] cubie : cubies) {
      byte[] p;
      if (i < num_edges / 2) {
        p = edges[0][i];
      } else {
        p = edges[1][i % 6];
      }

      s += arrayToString(cubie) + "=>" + arrayToString(p) + ", ";
      i++;
    }
    s = s.substring(0, s.length() - 2);
    s += "]";
    return s;
  }

  public static char getColor(byte color) {
    switch (color) {
      case YELLOW:
        return 'Y';
      case ORANGE:
        return 'O';
      case WHITE:
        return 'W';
      case RED:
        return 'R';
      case BLUE:
        return 'B';
      case GREEN:
        return 'G';
      default:
        return 0x0;
    }

  }

  public int[] getStates() {
    int[] states = new int[2];
    //System.out.println("Edge 1");
    rebuildDefaultEdgePositions1();
    states[0] = getEdgeState(edges[0], defaultEdgePositions1);
    // System.out.println("Edge 2");
    rebuildDefaultEdgePositions2();
    states[1] = getEdgeState(edges[1], defaultEdgePositions2);
    return states;
  }

  private int getEdgeState(byte[][] positions, LinkedList<byte[]> d) {
    int state = 0;
    int base = 1;
    for (int i = 0; i < positions.length; i++) {
      byte[] position = positions[i];
      int index = 0;
      while (!haveSameElements(d.get(index), position)) {
        index++;
      }
      int orientation = getEdgeOrientation(d.get(index), position);
      state += ((index * 2) + orientation) * base;
      base *= d.size() * 2;
      d.remove(index);
    }
    return state;
  }

  private static void rebuildDefaultEdgePositions1() {
    defaultEdgePositions1 = new LinkedList<>();
    for (int i = 0; i < num_edges; i++) {
      defaultEdgePositions1.add(Arrays.copyOf(cubies[i], cubies[i].length));
    }
  }

  private static void rebuildDefaultEdgePositions2() {
    defaultEdgePositions2 = new LinkedList<>();
    for (int i = 0; i < num_edges / 2; i++) {
      defaultEdgePositions2.add(Arrays.copyOf(cubies[num_edges / 2 + i], cubies[num_edges / 2 + i].length));
    }

    for (int i = 0; i < num_edges / 2; i++) {
      defaultEdgePositions2.add(Arrays.copyOf(cubies[i], cubies[i].length));
    }
  }

  private static boolean hasFace(byte[] position, byte face) {
    for (byte f : position) {
      if (f == face) {
        return true;
      }
    }
    return false;
  }

  private static void rotateAroundFace(byte[] position, byte face) {
    for (int i = 0; i < position.length; i++) {
      if (position[i] != face) {

        int j = 0;
        while (j < sequences[face].length && position[i] != sequences[face][j]) {
          j++;
        }
        position[i] = sequences[face][((j + 1) % sequences[face].length)];
      }
    }
  }

  private static boolean haveSameElements(byte[] a1, byte[] a2) {
    for (byte x : a1) {
      boolean found = false;
      for (byte y : a2) {
        if (x == y) {
          found = true;
        }
      }
      if (!found) {
        return false;
      }
    }

    return true;
  }

  private static String arrayToString(byte[] array) {
    String s = "";

    for (byte b : array) {
      s += getColor(b);
    }

    return s;
  }

  private static String arrayToString(LinkedList<byte[]> array) {
    String s = "";

    for (byte[] b : array) {
      s += arrayToString(b) + " ";
    }

    return s;
  }

  private static String arrayToString(byte[][] array) {
    String s = "";

    for (byte[] b : array) {
      s += arrayToString(b) + " ";
    }

    return s;
  }

  public boolean isSolved() {
    int[] states = getStates();
    for (int state : states) {
      if (state > 0) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof RubiksCubeEdges) && equals((RubiksCubeEdges) other);
  }

  public boolean equals(RubiksCubeEdges other) {
    return Arrays.deepEquals(edges, other.edges);
  }

  private int getEdgeOrientation(byte[] defaultPosition, byte[] position) {
    for (int i = 0; i < defaultPosition.length; i++) {
      if (defaultPosition[i] == position[0]) {
        return i;
      }
    }
    return -1;
  }
}