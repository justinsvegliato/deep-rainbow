package rubikscube;

import java.util.Arrays;
import java.util.LinkedList;

public class RubiksCube {

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
    {YELLOW, GREEN, RED},
    {YELLOW, BLUE, RED},
    {YELLOW, BLUE, ORANGE},
    {YELLOW, GREEN, ORANGE},
    {WHITE, BLUE, RED},
    {WHITE, GREEN, RED},
    {WHITE, GREEN, ORANGE},
    {WHITE, BLUE, ORANGE},
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
  private static final int num_corners = 8;
  private static final int num_edges = 12;
  private final byte[][] corners;
  private final byte[][][] edges;
  private static LinkedList<byte[]> defaultCornerPositions;
  private static LinkedList<byte[]> defaultEdgePositions1;
  private static LinkedList<byte[]> defaultEdgePositions2;

  public RubiksCube() {
    corners = new byte[num_corners][3];
    for (int i = 0; i < corners.length; i++) {
      corners[i] = Arrays.copyOf(cubies[i], 3);
    }

    edges = new byte[2][num_edges / 2][2];
    for (int i = 0; i < edges.length; i++) {
      for (int j = 0; j < edges[i].length; j++) {
        edges[i][j] = Arrays.copyOf(cubies[num_corners + i * (num_edges / 2) + j], 2);
      }
    }
  }

  public RubiksCube(RubiksCube cube) {
    corners = new byte[num_corners][3];
    for (int i = 0; i < corners.length; i++) {
      corners[i] = Arrays.copyOf(cube.corners[i], cube.corners[i].length);
    }

    edges = new byte[2][num_edges / 2][2];
    for (int i = 0; i < edges.length; i++) {
      for (int j = 0; j < edges[i].length; j++) {
        edges[i][j] = Arrays.copyOf(cube.edges[i][j], cube.edges[i][j].length);
      }
    }
  }

  private byte charToByte(char c) {
    switch (c) {
      case 'Y':
        return 0;
      case 'W':
        return 1;
      case 'G':
        return 2;
      case 'B':
        return 3;
      case 'R':
        return 4;
      case 'O':
        return 5;
      default:
        return 0x0;
    }
  }

  public RubiksCube(String representation) {
    byte[] cube = new byte[54];
    int index = 0;
    for (char c : representation.toCharArray()) {
      cube[index++] = charToByte(c);
    }

    byte[][] positions = {
      {12, 11, 6},
      {14, 15, 8},
      {32, 33, 38},
      {30, 29, 36},
      {53, 17, 2},
      {51, 9, 0},
      {45, 27, 42},
      {47, 35, 44},
      {13, 7},
      {23, 24},
      {31, 37},
      {21, 20},
      {10, 3},
      {28, 39},
      {52, 1},
      {48, 18},
      {46, 43},
      {50, 26},
      {16, 5},
      {34, 41}
    };

    corners = new byte[num_corners][3];
    edges = new byte[2][num_edges / 2][2];

    for (int i = 0; i < cubies.length; i++) {
      for (int j = 0; j < positions.length; j++) {
        byte[] position = new byte[positions[j].length];
        for (int k = 0; k < position.length; k++) {
          position[k] = cube[positions[j][k]];
        }

        if (haveSameElements(cubies[i], position)) {
          int h = 0;

          while (cubies[i][0] != position[h]) {
            h++;
          }

          position = orientPosition(cubies[j], h);

          if (i < num_corners) {
            corners[i] = position;
          } else if (i < num_corners + num_edges / 2) {
            edges[0][i - num_corners] = position;
          } else {
            edges[1][i - num_corners - num_edges / 2] = position;
          }
        }
      }
    }
  }

  public void rotateFace(byte face) {
    for (byte[] position : corners) {
      if (hasFace(position, face)) {
        rotateAroundFace(position, face);
      }
    }

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
      if (i < 8) {
        p = corners[i];
      } else if (i < num_corners + num_edges / 2) {
        p = edges[0][i - num_corners];
      } else {
        p = edges[1][i - num_corners - num_edges / 2];
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
    int[] states = new int[3];

    rebuildDefaultCornerPositions();
    states[0] = getCornerState(corners, defaultCornerPositions);
    rebuildDefaultEdgePositions1();
    states[1] = getEdgeState(edges[0], defaultEdgePositions1);
    rebuildDefaultEdgePositions2();
    states[2] = getEdgeState(edges[1], defaultEdgePositions2);
    return states;
  }

  private int getCornerState(byte[][] positions, LinkedList<byte[]> defaults) {
    int state = 0;
    int base = 1;
    for (int i = 0; i < positions.length - 1; i++) {
      byte[] position = positions[i];
      int index = 0;
      while (!haveSameElements(defaults.get(index), position)) {
        index++;
      }
      state += ((index * 3) + (position[0] / 2)) * base;
      base *= defaults.size() * 3;
      defaults.remove(index);
    }
    return state;
  }

  private int getEdgeState(byte[][] positions, LinkedList<byte[]> defaults) {
    int state = 0;
    int base = 1;
    for (int i = 0; i < positions.length; i++) {
      byte[] position = positions[i];
      int index = 0;
      while (!haveSameElements(defaults.get(index), position)) {
        index++;
      }
      int orientation = getEdgeOrientation(defaults.get(index), position);
      state += ((index * 2) + orientation) * base;
      base *= defaults.size() * 2;
      defaults.remove(index);
    }
    return state;
  }

  private static void rebuildDefaultCornerPositions() {
    defaultCornerPositions = new LinkedList<>();
    for (int i = 0; i < 8; i++) {
      defaultCornerPositions.add(Arrays.copyOf(cubies[i], cubies[i].length));
    }
  }

  private static void rebuildDefaultEdgePositions1() {
    defaultEdgePositions1 = new LinkedList<>();
    for (int i = 0; i < num_edges; i++) {
      defaultEdgePositions1.add(Arrays.copyOf(cubies[num_corners + i], cubies[num_corners + i].length));
    }
  }

  private static void rebuildDefaultEdgePositions2() {
    defaultEdgePositions2 = new LinkedList<>();
    for (int i = 0; i < num_edges / 2; i++) {
      defaultEdgePositions2.add(Arrays.copyOf(cubies[num_corners + num_edges / 2 + i], cubies[num_corners + num_edges / 2 + i].length));
    }

    for (int i = 0; i < num_edges / 2; i++) {
      defaultEdgePositions2.add(Arrays.copyOf(cubies[num_corners + i], cubies[num_corners + i].length));
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
    if (a1.length != a2.length) {
      return false;
    }
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
    return (other instanceof RubiksCube) && equals((RubiksCube) other);
  }

  public boolean equals(RubiksCube other) {
    return Arrays.deepEquals(corners, other.corners) && Arrays.deepEquals(edges, other.edges);
  }

  private int getEdgeOrientation(byte[] defaultPosition, byte[] position) {
    for (int i = 0; i < defaultPosition.length; i++) {
      if (defaultPosition[i] == position[0]) {
        return i;
      }
    }
    return -1;
  }

  private byte[] orientPosition(byte[] p, int rotations) {
    byte[] position = Arrays.copyOf(p, p.length);
    for (int i = 0; i < rotations; i++) {
      for (int j = 0; j < position.length - 1; j++) {
        byte temp = position[j];
        position[j] = position[j + 1];
        position[j + 1] = temp;
      }
    }
    return position;
  }
}