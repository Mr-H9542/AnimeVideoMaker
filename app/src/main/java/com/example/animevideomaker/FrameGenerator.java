public static List<VideoFrame> generate(Context ctx, Scene scene) {
    if (scene == null) {
        throw new IllegalArgumentException("Scene cannot be null");
    }
    Bitmap bg = scene.getBackground();
    if (bg == null) {
        throw new IllegalArgumentException("Scene background cannot be null");
    }
    List<Character> chars = scene.getCharactersByDepth();
    if (chars == null || chars.isEmpty()) {
        throw new IllegalArgumentException("Scene must have at least one Character");
    }

    List<VideoFrame> frames = new ArrayList<>();
    CharacterRenderer renderer = new CharacterRenderer();  // <== no args now
    Character mainChar = chars.get(0);

    int totalFrames = scene.getDuration() * FPS;

    for (int i = 0; i < totalFrames; i++) {
        Bitmap frameBitmap = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(frameBitmap);
        canvas.drawBitmap(bg, 0, 0, null);

        Bitmap charBitmap = renderer.renderCharacterFrame(mainChar, bg.getWidth(), bg.getHeight(), i, totalFrames);
        canvas.drawBitmap(charBitmap, 0, 0, null);

        frames.add(new VideoFrame(frameBitmap, i));

        if (charBitmap != frameBitmap && !charBitmap.isRecycled()) {
            charBitmap.recycle();
        }
    }

    return frames;
}
