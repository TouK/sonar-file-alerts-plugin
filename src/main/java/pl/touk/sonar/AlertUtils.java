package pl.touk.sonar;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.profiles.Alert;

/**
 * This is a copy of AlertUtils - it's an example of anti-pattern called Weak Domain Model - Alert class is weak and you need AlertUtils to execute methods that belong to Alert.
 * ttps://github.com/SonarSource/sonar/blob/master/plugins/sonar-core-plugin/src/main/java/org/sonar/plugins/core/sensors/AlertUtils.java
 */
public final class AlertUtils {

  private AlertUtils() {
  }

  /**
   * Get the matching alert level for the given measure
   */
  public static Metric.Level getLevel(Alert alert, Measure measure) {
    if (evaluateAlert(alert, measure, Metric.Level.ERROR)) {
      return Metric.Level.ERROR;
    }
    if (evaluateAlert(alert, measure, Metric.Level.WARN)) {
      return Metric.Level.WARN;
    }
    return Metric.Level.OK;
  }

  private static boolean evaluateAlert(Alert alert, Measure measure, Metric.Level alertLevel) {
    String valueToEval = getValueToEval(alert, alertLevel);
    if (StringUtils.isEmpty(valueToEval)) {
      return false;
    }

    Comparable criteriaValue = getValueForComparison(alert.getMetric(), valueToEval);
    Comparable measureValue = getMeasureValue(alert, measure);
    if (measureValue != null) {
      return doesReachThresholds(measureValue, criteriaValue, alert);
    }
    return false;
  }

  private static boolean doesReachThresholds(Comparable measureValue, Comparable criteriaValue, Alert alert) {
    int comparison = measureValue.compareTo(criteriaValue);
    return !(isNotEquals(comparison, alert)
        || isGreater(comparison, alert)
        || isSmaller(comparison, alert)
        || isEquals(comparison, alert));
  }

  private static boolean isNotEquals(int comparison, Alert alert) {
    return alert.isNotEqualsOperator() && comparison == 0;
  }

  private static boolean isGreater(int comparison, Alert alert) {
    return alert.isGreaterOperator() && comparison != 1;
  }

  private static boolean isSmaller(int comparison, Alert alert) {
    return alert.isSmallerOperator() && comparison != -1;
  }

  private static boolean isEquals(int comparison, Alert alert) {
    return alert.isEqualsOperator() && comparison != 0;
  }

  private static String getValueToEval(Alert alert, Metric.Level alertLevel) {
    if (alertLevel.equals(Metric.Level.ERROR)) {
      return alert.getValueError();
    } else if (alertLevel.equals(Metric.Level.WARN)) {
      return alert.getValueWarning();
    } else {
      throw new IllegalStateException(alertLevel.toString());
    }
  }

  private static Comparable<?> getValueForComparison(Metric metric, String value) {
    if (isADouble(metric)) {
      return Double.parseDouble(value);
    }
    if (isAInteger(metric)) {
      return parseInteger(value);
    }
    if (isAString(metric)) {
      return value;
    }
    if (isABoolean(metric)) {
      return Integer.parseInt(value);
    }
    throw new NotImplementedException(metric.getType().toString());
  }

  private static Comparable<Integer> parseInteger(String value) {
    return value.contains(".") ? Integer.parseInt(value.substring(0, value.indexOf('.'))) : Integer.parseInt(value);
  }

  private static Comparable<?> getMeasureValue(Alert alert, Measure measure) {
    Metric metric = alert.getMetric();
    if (isADouble(metric)) {
      return getValue(alert, measure);
    }
    if (isAInteger(metric)) {
      return parseInteger(alert, measure);
    }
    if (alert.getPeriod() == null) {
      return getMeasureValueForStringOrBoolean(metric, measure);
    }
    throw new NotImplementedException(metric.getType().toString());
  }

  private static Comparable<?> getMeasureValueForStringOrBoolean(Metric metric, Measure measure) {
    if (isAString(metric)) {
      return measure.getData();
    }
    if (isABoolean(metric)) {
      return measure.getValue().intValue();
    }
    throw new NotImplementedException(metric.getType().toString());
  }

  private static Comparable<Integer> parseInteger(Alert alert, Measure measure) {
    Double value = getValue(alert, measure);
    return value != null ? value.intValue() : null;
  }

  private static boolean isADouble(Metric metric) {
    return metric.getType() == Metric.ValueType.FLOAT ||
        metric.getType() == Metric.ValueType.PERCENT ||
        metric.getType() == Metric.ValueType.RATING;
  }

  private static boolean isAInteger(Metric metric) {
    return metric.getType() == Metric.ValueType.INT ||
        metric.getType() == Metric.ValueType.MILLISEC;
  }

  private static boolean isAString(Metric metric) {
    return metric.getType() == Metric.ValueType.STRING ||
        metric.getType() == Metric.ValueType.LEVEL;
  }

  private static boolean isABoolean(Metric metric) {
    return metric.getType() == Metric.ValueType.BOOL;
  }

  private static Double getValue(Alert alert, Measure measure) {
    if (alert.getPeriod() == null) {
      return measure.getValue();
    } else if (alert.getPeriod() == 1) {
      return measure.getVariation1();
    } else if (alert.getPeriod() == 2) {
      return measure.getVariation2();
    } else if (alert.getPeriod() == 3) {
      return measure.getVariation3();
    } else {
      throw new IllegalStateException("Following index period is not allowed : " + Double.toString(alert.getPeriod()));
    }
  }
}
