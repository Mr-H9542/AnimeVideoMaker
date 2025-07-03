public class Scene implements Serializable {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());

    private final int width;
    private final int height;
    private transient Bitmap background;
    private final List<Character> characters = new ArrayList<>();
    private int durationSeconds = 5;

    public Scene() {
        this(720, 1280);
    }

    public Scene(int width, int height) {
        this.width = width;
        this.height = height;
        setBackgroundColor("black");
    }

    public void setBackgroundColor(String colorName) {
        int color = switch (colorName.toLowerCase()) {
            case "white" -> Color.WHITE;
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "gray" -> Color.GRAY;
            default -> Color.BLACK;
        };
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        new Canvas(bmp).drawColor(color);
        this.background = bmp;
    }

    public void replaceWithCharacter(Character character) {
        characters.clear();
        if (character != null) {
            characters.add(character);
        }
    }

    public void configureFromRequest(AnimationRequest req) {
        if (req == null) return;

        String bgColor = req.background == null || req.background.equalsIgnoreCase("default")
                ? new AnimationRequest().background
                : req.background;

        setBackgroundColor(bgColor);

        Character character = new Character(
                "char_" + System.currentTimeMillis(),
                req.characterType,
                req.characterColor,
                req.action,
                new PointF(100, 100),
                true
        );

        replaceWithCharacter(character);
        setDuration(req.duration);
    }

    public void generateFrames(Context context, Dialog loadingDialog, Runnable onComplete) {
        if (loadingDialog != null) {
            uiHandler.post(loadingDialog::show);
        }

        executor.execute(() -> {
            try {
                Thread.sleep(durationSeconds * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            uiHandler.post(() -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                if (onComplete != null) onComplete.run();
            });
        });
    }
}
