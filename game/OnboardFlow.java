import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OnboardFlow {
    private final List<OnboardStep> steps;

    public OnboardFlow(List<OnboardStep> steps) {
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
    }

    public int size() {
        return steps.size();
    }

    public OnboardStep get(int index) {
        return steps.get(index);
    }
}
