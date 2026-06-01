/**
 * A {@link Level} built from a {@link LevelDefinition} parsed at runtime (pasted
 * {@code .lvl} text or a base64 share-code), rather than from an embedded built-in
 * document. Used by Free Play; it installs through the same path as every built-in
 * level so there is no separate custom rendering code.
 */
public class CustomLevel implements Level {

    private final LevelDefinition definition;

    public CustomLevel(LevelDefinition definition) {
        this.definition = definition;
    }

    @Override
    public void setup(SimulationWorld world) {
        world.installLevelDefinition(definition);
    }
}
