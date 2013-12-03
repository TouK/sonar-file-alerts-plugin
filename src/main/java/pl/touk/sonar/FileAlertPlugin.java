package pl.touk.sonar;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;

import java.util.Arrays;
import java.util.List;

@Properties({
        @Property(key = PropertyKey.FILE_ALERT_ENABLED, name = PropertyKey.FILE_ALERT_ENABLED, defaultValue = "false", type = PropertyType.BOOLEAN)
})

public final class FileAlertPlugin extends SonarPlugin {
    @SuppressWarnings("unchecked")
    public List<?> getExtensions() {
        return Arrays.asList(FileAlertDecorator.class);
    }
}