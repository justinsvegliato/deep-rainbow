package rubikscube;

import java.util.Arrays;
import java.util.LinkedList;

public class RubiksCubeCorners {

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
    {WHITE, BLUE, ORANGE}
  };
  private static final int num_corners = 8;
  private final byte[][] corners;
  private static LinkedList<byte[]> defaults;

  public RubiksCubeCorners() {
    corners = new byte[num_corners][3];
    for (int i = 0; i < corners.length; i++) {
      corners[i] = Arrays.copyOf(cubies[i], 3);
    }
  }

  public RubiksCubeCorners(RubiksCubeCorners cube) {
    corners = new byte[num_corners][3];
    for (int i = 0; i < corners.length; i++) {
      corners[i] = Arrays.copyOf(cube.corners[i], cube.corners[i].length);
    }
  }

  public void rotateFace(byte face) {
    for (byte[] position : corners) {
      if (hasFace(position, face)) {
        rotateAroundFace(position, face);
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
      s += arrayToString(cubie) + "=>" + arrayToString(corners[i]) + ", ";
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

  public int getState() {
    rebuildDefaults();
    return getCornerState(corners, defaults);

  }

  private int getCornerState(byte[][] positions, LinkedList<byte[]> d) {
    int state = 0;
    int base = 1;
    for (int i = 0; i < positions.length - 1; i++) {
      byte[] position = positions[i];
      int index = 0;
      while (!haveSameElements(d.get(index), position)) {
        index++;
      }
      state += ((index * 3) + (position[0] / 2)) * base;
      base *= d.size() * 3;
      d.remove(index);
    }
    return state;
  }

  private static void rebuildDefaults() {
    defaults = new LinkedList<>();
    for (int i = 0; i < 8; i++) {
      defaults.add(Arrays.copyOf(cubies[i], cubies[i].length));
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
}