package oucomp.textanalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfusionMatrix {

  private List<String> categoryList = new ArrayList();
  private HashMap<String, Integer> categoryIndexMap = new HashMap();
  private int matrix[][] = new int[12][12];

  private synchronized int getCategoryIndex(String category) {
    if (!categoryIndexMap.containsKey(category)) {
      int index = categoryIndexMap.size();
      categoryIndexMap.put(category, index);
      categoryList.add(category);
      fixMatrixSize();
      return index;
    } else {
      int index = categoryIndexMap.get(category);
      return index;
    }
  }

  private synchronized void fixMatrixSize() {
    int size = categoryIndexMap.size();
    if (size > matrix.length) {
      int copy[][] = new int[matrix.length * 2][matrix.length * 2];
      for (int i = 0; i < matrix.length; i++) {
        System.arraycopy(matrix[i], 0, copy[i], 0, matrix.length);
      }
      matrix = copy;
    }
  }

  public synchronized void addValueToMatrix(String queryCategory, String resultCategory, int value) {
    int queryIndex = getCategoryIndex(queryCategory);
    int resultIndex = getCategoryIndex(resultCategory);
    matrix[queryIndex][resultIndex] += value;
  }

  public void printMatrix() {
    int categoryCount = categoryList.size();
    for (int i = 0; i < categoryCount; i++) {
      for (int j = 0; j < categoryCount; j++) {
        System.out.printf("| %4d ", matrix[i][j]);
      }
      System.out.println("|");
    }
  }
}
