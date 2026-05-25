import greenfoot.*;

public class FinalClassificationWorld extends World {
    public FinalClassificationWorld() {
        super(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT, 1);
        setBackground(new GreenfootImage(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT));
        drawUI();
        addObject(new MenuButton("RESTART", () -> {
            LevelManager.resetProgress();
            Greenfoot.setWorld(new MyWorld(LevelManager.getCurrentLevel()));
        }), GameScreenLayout.WORLD_WIDTH / 2 - GameScreenLayout.scale(88), GameScreenLayout.scale(642));
        addObject(new MenuButton("HOME", () -> Greenfoot.setWorld(new HomeWorld())),
            GameScreenLayout.WORLD_WIDTH / 2 + GameScreenLayout.scale(92), GameScreenLayout.scale(642));
    }

    private void drawUI() {
        GreenfootImage bg = getBackground();
        int totalStars = LevelManager.getTotalStars();
        String type = classificationName(totalStars);
        String clearance = clearanceText(totalStars);

        int cardX = GameScreenLayout.scale(96);
        int cardY = GameScreenLayout.scale(56);
        int cardW = GameScreenLayout.WORLD_WIDTH - GameScreenLayout.scale(192);
        int cardH = GameScreenLayout.scale(540);
        int contentX = cardX + GameScreenLayout.scale(58);
        int contentW = cardW - GameScreenLayout.scale(116);

        bg.setColor(new Color(20, 24, 31));
        bg.fill();
        bg.setColor(new Color(241, 242, 236));
        bg.fillRect(cardX, cardY, cardW, cardH);
        bg.setColor(new Color(217, 222, 214));
        bg.fillRect(cardX, cardY, cardW, GameScreenLayout.scale(102));
        bg.setColor(new Color(66, 78, 88));
        bg.drawRect(cardX, cardY, cardW, cardH);
        bg.drawRect(cardX + 1, cardY + 1, cardW - 2, cardH - 2);

        bg.setColor(new Color(42, 50, 58));
        bg.setFont(new Font("SansSerif", true, false, GameScreenLayout.scale(15)));
        bg.drawString("PROJECT RIVETS // TRAINING COMPLETE", contentX, cardY + GameScreenLayout.scale(40));
        bg.setFont(new Font("SansSerif", true, false, GameScreenLayout.scale(30)));
        bg.drawString(type, contentX, cardY + GameScreenLayout.scale(86));

        drawScoreBox(bg, totalStars, contentX, cardY + GameScreenLayout.scale(138), contentW);

        bg.setFont(new Font("SansSerif", false, false, GameScreenLayout.scale(17)));
        drawWrapped(bg, clearance, contentX, cardY + GameScreenLayout.scale(256), contentW, GameScreenLayout.scale(25), new Color(42, 50, 58), 4);

        bg.setFont(new Font("SansSerif", false, false, GameScreenLayout.scale(14)));
        drawWrapped(bg,
            "Classification thresholds: Type III = 40-45 stars, Type II = 28-39 stars, Type I = 15-27 stars. Training below 15 stars remains incomplete.",
            contentX, cardY + GameScreenLayout.scale(406), contentW, GameScreenLayout.scale(21), new Color(82, 92, 98), 4);
    }

    private void drawScoreBox(GreenfootImage bg, int totalStars, int x, int y, int width) {
        bg.setColor(new Color(28, 31, 38));
        bg.fillRect(x, y, width, GameScreenLayout.scale(74));
        bg.setColor(new Color(106, 130, 158));
        bg.drawRect(x, y, width, GameScreenLayout.scale(74));
        bg.setColor(new Color(238, 239, 232));
        bg.setFont(new Font("SansSerif", true, false, GameScreenLayout.scale(18)));
        bg.drawString("Aggregate Star Record", x + GameScreenLayout.scale(24), y + GameScreenLayout.scale(28));
        bg.setFont(new Font("SansSerif", true, false, GameScreenLayout.scale(24)));
        bg.drawString(totalStars + " / " + LevelManager.getMaxStars(), x + GameScreenLayout.scale(24), y + GameScreenLayout.scale(60));
    }

    private String classificationName(int stars) {
        if (stars >= 40) return "TYPE III - EMERGENT ARCHITECT";
        if (stars >= 28) return "TYPE II - ADAPTIVE SYSTEM";
        if (stars >= 15) return "TYPE I - FUNCTIONAL EXECUTOR";
        return "TRAINING INCOMPLETE";
    }

    private String clearanceText(int stars) {
        if (stars >= 40) {
            return "RIVETS consistently received concise, structured programs. The simulation board recommends full autonomous deployment clearance.";
        }
        if (stars >= 28) {
            return "RIVETS demonstrated reliable control with partial optimisation. Deployment is approved under supervised conditions.";
        }
        if (stars >= 15) {
            return "RIVETS can execute correct instructions, but the control record remains rigid. Clearance is limited to controlled environments.";
        }
        return "RIVETS reached the final gate, but the training record is not yet strong enough for deployment clearance.";
    }

    private void drawWrapped(GreenfootImage image, String text, int x, int y, int width, int lineHeight, Color color, int maxLines) {
        int maxChars = Math.max(24, width / Math.max(1, GameScreenLayout.scale(8)));
        String[] words = text.split(" ");
        String line = "";
        int lineIndex = 0;
        image.setColor(color);
        for (String word : words) {
            if (line.length() == 0) {
                line = word;
            } else if (line.length() + 1 + word.length() <= maxChars) {
                line += " " + word;
            } else {
                if (lineIndex >= maxLines) return;
                image.drawString(line, x, y + lineIndex * lineHeight);
                lineIndex++;
                line = word;
            }
        }
        if (line.length() > 0 && lineIndex < maxLines) {
            image.drawString(line, x, y + lineIndex * lineHeight);
        }
    }
}
