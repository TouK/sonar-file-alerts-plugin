package pl.touk.sonar;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.*;
import org.sonar.api.config.Settings;
import org.sonar.api.i18n.I18n;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.profiles.Alert;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;

import java.util.List;
import java.util.Locale;


/**
 * This decorator is mainly a copy-paste of CheckAlertThresholds, because it contained most private methods.
 * Periods are removed, because it is not available in this API.
 * https://github.com/SonarSource/sonar/blob/master/plugins/sonar-core-plugin/src/main/java/org/sonar/plugins/core/sensors/CheckAlertThresholds.java?source=c
 */
public class FileAlertDecorator implements Decorator {
    private static final Logger LOG = LoggerFactory.getLogger(FileAlertDecorator.class);
    private static final String VARIATION_METRIC_PREFIX = "new_";
    private static final String VARIATION = "variation";
    private RulesProfile profile;
    private I18n i18n;
    private boolean enabled;

    public FileAlertDecorator(Settings settings, RulesProfile profile, I18n i18n) {
        this.enabled = settings.getBoolean(PropertyKey.FILE_ALERTS_ENABLED);
        this.profile = profile;
        this.i18n = i18n;
    }

    @DependedUpon
    public Metric generatesAlertStatus() {
        return CoreMetrics.ALERT_STATUS;
    }

    @DependsUpon
    public String dependsOnVariations() {
        return DecoratorBarriers.END_OF_TIME_MACHINE;
    }

    @DependsUpon
    public List<Metric> dependsUponMetrics() {
        List<Metric> metrics = Lists.newLinkedList();
        for (Alert alert : profile.getAlerts()) {
            metrics.add(alert.getMetric());
        }
        return metrics;
    }


    public boolean shouldExecuteOnProject(Project project) {
        return enabled
                && profile != null
                && profile.getAlerts() != null
                && profile.getAlerts().size() > 0;
    }

    public void decorate(final Resource resource, final DecoratorContext context) {
        if (shouldDecorateResource(resource)) {
            decorateResource(context);
        }
    }

    private void decorateResource(DecoratorContext context) {
        for (final Alert alert : profile.getAlerts()) {
            Measure measure = context.getMeasure(alert.getMetric());
            if (measure == null) {
                return;
            }

            Metric.Level level = AlertUtils.getLevel(alert, measure);
            if (level == Metric.Level.OK) {
                return;
            }

            LOG.debug("Alert raised on file {}: {} with level {}", context.getResource(), alert.getMetric().getName(), level);
            measure.setAlertStatus(level);
            measure.setAlertText(getText(alert, level));
            context.saveMeasure(measure);
        }
    }

    private boolean shouldDecorateResource(final Resource resource) {
        return ResourceUtils.isFile(resource);
    }

    private String getText(Alert alert, Metric.Level level) {
        if (level == Metric.Level.OK) {
            return null;
        }
        return getAlertLabel(alert, level);
    }

    private String getAlertLabel(Alert alert, Metric.Level level) {
        Integer alertPeriod = alert.getPeriod();
        String metric = i18n.message(getLocale(), "metric." + alert.getMetric().getKey() + ".name", alert.getMetric().getName());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(metric);

        if (alertPeriod != null && !alert.getMetric().getKey().startsWith(VARIATION_METRIC_PREFIX)) {
            String variation = i18n.message(getLocale(), VARIATION, VARIATION).toLowerCase();
            stringBuilder.append(" ").append(variation);
        }

        stringBuilder
                .append(" ").append(alert.getOperator()).append(" ")
                .append(level.equals(Metric.Level.ERROR) ? alert.getValueError() : alert.getValueWarning());

//        if (alertPeriod != null) {
//            stringBuilder.append(" ").append(periods.label(snapshot, alertPeriod));
//        }

        return stringBuilder.toString();
    }


    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    private Locale getLocale() {
        return Locale.ENGLISH;
    }
}
