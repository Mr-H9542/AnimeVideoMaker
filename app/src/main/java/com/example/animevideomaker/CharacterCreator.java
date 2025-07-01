private List<Character> loadCharacters() {
    List<Character> characters = new ArrayList<>();
    try {
        String[] folders = assetManager.list("characters");
        if (folders != null) {
            for (String folder : folders) {
                Character character = new Character(
                    folder,
                    "star",
                    "blue",
                    "idle",
                    new android.graphics.PointF(0, 0),
                    false
                );
                characters.add(character);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return characters;
}
