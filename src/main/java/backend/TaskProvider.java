package backend;

import java.util.Collection;

public interface TaskProvider {

    Collection<TaskBase> get_tasks();

}
