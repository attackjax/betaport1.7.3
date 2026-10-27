package dev.b173.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BetaRecipes {
    public static final String[] LINES = {
            "shaped 3 3 270:0*1 5:-1 5:-1 5:-1 - 280:0 - - 280:0 -",
            "shaped 3 3 274:0*1 4:-1 4:-1 4:-1 - 280:0 - - 280:0 -",
            "shaped 3 3 257:0*1 265:0 265:0 265:0 - 280:0 - - 280:0 -",
            "shaped 3 3 278:0*1 264:0 264:0 264:0 - 280:0 - - 280:0 -",
            "shaped 3 3 285:0*1 266:0 266:0 266:0 - 280:0 - - 280:0 -",
            "shaped 3 3 261:0*1 - 280:0 287:0 280:0 - 287:0 - 280:0 287:0",
            "shaped 3 3 41:0*1 266:0 266:0 266:0 266:0 266:0 266:0 266:0 266:0 266:0",
            "shaped 3 3 42:0*1 265:0 265:0 265:0 265:0 265:0 265:0 265:0 265:0 265:0",
            "shaped 3 3 57:0*1 264:0 264:0 264:0 264:0 264:0 264:0 264:0 264:0 264:0",
            "shaped 3 3 22:0*1 351:4 351:4 351:4 351:4 351:4 351:4 351:4 351:4 351:4",
            "shaped 3 3 54:0*1 5:-1 5:-1 5:-1 5:-1 - 5:-1 5:-1 5:-1 5:-1",
            "shaped 3 3 61:0*1 4:-1 4:-1 4:-1 4:-1 - 4:-1 4:-1 4:-1 4:-1",
            "shaped 3 3 299:0*1 334:0 - 334:0 334:0 334:0 334:0 334:0 334:0 334:0",
            "shaped 3 3 300:0*1 334:0 334:0 334:0 334:0 - 334:0 334:0 - 334:0",
            "shaped 3 3 303:0*1 51:-1 - 51:-1 51:-1 51:-1 51:-1 51:-1 51:-1 51:-1",
            "shaped 3 3 304:0*1 51:-1 51:-1 51:-1 51:-1 - 51:-1 51:-1 - 51:-1",
            "shaped 3 3 307:0*1 265:0 - 265:0 265:0 265:0 265:0 265:0 265:0 265:0",
            "shaped 3 3 308:0*1 265:0 265:0 265:0 265:0 - 265:0 265:0 - 265:0",
            "shaped 3 3 311:0*1 264:0 - 264:0 264:0 264:0 264:0 264:0 264:0 264:0",
            "shaped 3 3 312:0*1 264:0 264:0 264:0 264:0 - 264:0 264:0 - 264:0",
            "shaped 3 3 315:0*1 266:0 - 266:0 266:0 266:0 266:0 266:0 266:0 266:0",
            "shaped 3 3 316:0*1 266:0 266:0 266:0 266:0 - 266:0 266:0 - 266:0",
            "shaped 3 3 84:0*1 5:-1 5:-1 5:-1 5:-1 264:0 5:-1 5:-1 5:-1 5:-1",
            "shaped 3 3 25:0*1 5:-1 5:-1 5:-1 5:-1 331:0 5:-1 5:-1 5:-1 5:-1",
            "shaped 3 3 47:0*1 5:-1 5:-1 5:-1 340:0 340:0 340:0 5:-1 5:-1 5:-1",
            "shaped 3 3 46:0*1 289:0 12:-1 289:0 12:-1 289:0 12:-1 289:0 12:-1 289:0",
            "shaped 3 3 65:0*2 280:0 - 280:0 280:0 280:0 280:0 280:0 - 280:0",
            "shaped 3 3 323:0*1 5:-1 5:-1 5:-1 5:-1 5:-1 5:-1 - 280:0 -",
            "shaped 3 3 354:0*1 335:0 335:0 335:0 353:0 344:0 353:0 296:0 296:0 296:0",
            "shaped 3 3 66:0*16 265:0 - 265:0 265:0 280:0 265:0 265:0 - 265:0",
            "shaped 3 3 27:0*6 266:0 - 266:0 266:0 280:0 266:0 266:0 331:0 266:0",
            "shaped 3 3 28:0*6 265:0 - 265:0 265:0 70:-1 265:0 265:0 331:0 265:0",
            "shaped 3 3 53:0*4 5:-1 - - 5:-1 5:-1 - 5:-1 5:-1 5:-1",
            "shaped 3 3 346:0*1 - - 280:0 - 280:0 287:0 280:0 - 287:0",
            "shaped 3 3 67:0*4 4:-1 - - 4:-1 4:-1 - 4:-1 4:-1 4:-1",
            "shaped 3 3 321:0*1 280:0 280:0 280:0 280:0 35:-1 280:0 280:0 280:0 280:0",
            "shaped 3 3 322:0*1 41:-1 41:-1 41:-1 41:-1 260:0 41:-1 41:-1 41:-1 41:-1",
            "shaped 3 3 347:0*1 - 266:0 - 266:0 331:0 266:0 - 266:0 -",
            "shaped 3 3 345:0*1 - 265:0 - 265:0 331:0 265:0 - 265:0 -",
            "shaped 3 3 358:0*1 339:0 339:0 339:0 339:0 345:0 339:0 339:0 339:0 339:0",
            "shaped 3 3 23:0*1 4:-1 4:-1 4:-1 4:-1 261:0 4:-1 4:-1 331:0 4:-1",
            "shaped 3 3 33:0*1 5:-1 5:-1 5:-1 4:-1 265:0 4:-1 4:-1 331:0 4:-1",
            "shaped 2 3 271:0*1 5:-1 5:-1 5:-1 280:0 - 280:0",
            "shaped 2 3 290:0*1 5:-1 5:-1 - 280:0 - 280:0",
            "shaped 2 3 275:0*1 4:-1 4:-1 4:-1 280:0 - 280:0",
            "shaped 2 3 291:0*1 4:-1 4:-1 - 280:0 - 280:0",
            "shaped 2 3 258:0*1 265:0 265:0 265:0 280:0 - 280:0",
            "shaped 2 3 292:0*1 265:0 265:0 - 280:0 - 280:0",
            "shaped 2 3 279:0*1 264:0 264:0 264:0 280:0 - 280:0",
            "shaped 2 3 293:0*1 264:0 264:0 - 280:0 - 280:0",
            "shaped 2 3 286:0*1 266:0 266:0 266:0 280:0 - 280:0",
            "shaped 2 3 294:0*1 266:0 266:0 - 280:0 - 280:0",
            "shaped 3 2 298:0*1 334:0 334:0 334:0 334:0 - 334:0",
            "shaped 3 2 301:0*1 334:0 - 334:0 334:0 - 334:0",
            "shaped 3 2 302:0*1 51:-1 51:-1 51:-1 51:-1 - 51:-1",
            "shaped 3 2 305:0*1 51:-1 - 51:-1 51:-1 - 51:-1",
            "shaped 3 2 306:0*1 265:0 265:0 265:0 265:0 - 265:0",
            "shaped 3 2 309:0*1 265:0 - 265:0 265:0 - 265:0",
            "shaped 3 2 310:0*1 264:0 264:0 264:0 264:0 - 264:0",
            "shaped 3 2 313:0*1 264:0 - 264:0 264:0 - 264:0",
            "shaped 3 2 314:0*1 266:0 266:0 266:0 266:0 - 266:0",
            "shaped 3 2 317:0*1 266:0 - 266:0 266:0 - 266:0",
            "shaped 3 2 85:0*2 280:0 280:0 280:0 280:0 280:0 280:0",
            "shaped 2 3 324:0*1 5:-1 5:-1 5:-1 5:-1 5:-1 5:-1",
            "shaped 3 2 96:0*2 5:-1 5:-1 5:-1 5:-1 5:-1 5:-1",
            "shaped 2 3 330:0*1 265:0 265:0 265:0 265:0 265:0 265:0",
            "shaped 3 2 281:0*4 5:-1 - 5:-1 - 5:-1 -",
            "shaped 3 2 328:0*1 265:0 - 265:0 265:0 265:0 265:0",
            "shaped 3 2 333:0*1 5:-1 - 5:-1 5:-1 5:-1 5:-1",
            "shaped 3 2 325:0*1 265:0 - 265:0 - 265:0 -",
            "shaped 3 2 356:0*1 76:-1 331:0 76:-1 1:-1 1:-1 1:-1",
            "shaped 3 2 355:0*1 35:-1 35:-1 35:-1 5:-1 5:-1 5:-1",
            "shaped 2 2 359:0*1 - 265:0 265:0 -",
            "shaped 2 2 58:0*1 5:-1 5:-1 5:-1 5:-1",
            "shaped 2 2 24:0*1 12:-1 12:-1 12:-1 12:-1",
            "shaped 2 2 80:0*1 332:0 332:0 332:0 332:0",
            "shaped 2 2 82:0*1 337:0 337:0 337:0 337:0",
            "shaped 2 2 45:0*1 336:0 336:0 336:0 336:0",
            "shaped 2 2 89:0*1 348:0 348:0 348:0 348:0",
            "shaped 2 2 35:0*1 287:0 287:0 287:0 287:0",
            "shaped 2 2 259:0*1 265:0 - - 318:0",
            "shaped 1 3 269:0*1 5:-1 280:0 280:0",
            "shaped 1 3 273:0*1 4:-1 280:0 280:0",
            "shaped 1 3 256:0*1 265:0 280:0 280:0",
            "shaped 1 3 277:0*1 264:0 280:0 280:0",
            "shaped 1 3 284:0*1 266:0 280:0 280:0",
            "shaped 1 3 268:0*1 5:-1 5:-1 280:0",
            "shaped 1 3 272:0*1 4:-1 4:-1 280:0",
            "shaped 1 3 267:0*1 265:0 265:0 280:0",
            "shaped 1 3 276:0*1 264:0 264:0 280:0",
            "shaped 1 3 283:0*1 266:0 266:0 280:0",
            "shaped 1 3 262:0*4 318:0 280:0 288:0",
            "shaped 1 3 282:0*1 40:-1 39:-1 281:0",
            "shaped 1 3 282:0*1 39:-1 40:-1 281:0",
            "shaped 3 1 357:0*8 296:0 351:3 296:0",
            "shaped 3 1 339:0*3 338:0 338:0 338:0",
            "shaped 1 3 340:0*1 339:0 339:0 339:0",
            "shaped 3 1 44:3*3 4:-1 4:-1 4:-1",
            "shaped 3 1 44:0*3 1:-1 1:-1 1:-1",
            "shaped 3 1 44:1*3 24:-1 24:-1 24:-1",
            "shaped 3 1 44:2*3 5:-1 5:-1 5:-1",
            "shaped 3 1 297:0*1 296:0 296:0 296:0",
            "shaped 1 2 280:0*4 5:-1 5:-1",
            "shaped 1 2 50:0*4 263:0 280:0",
            "shaped 1 2 50:0*4 263:1 280:0",
            "shaped 1 2 91:0*1 86:-1 50:-1",
            "shaped 1 2 342:0*1 54:-1 328:0",
            "shaped 1 2 343:0*1 61:-1 328:0",
            "shaped 1 2 69:0*1 280:0 4:-1",
            "shaped 1 2 76:0*1 331:0 280:0",
            "shaped 1 2 77:0*1 1:-1 1:-1",
            "shaped 2 1 70:0*1 1:-1 1:-1",
            "shaped 2 1 72:0*1 5:-1 5:-1",
            "shaped 1 2 29:0*1 341:0 33:-1",
            "shaped 1 1 266:0*9 41:-1",
            "shaped 1 1 265:0*9 42:-1",
            "shaped 1 1 264:0*9 57:-1",
            "shaped 1 1 351:4*9 22:-1",
            "shaped 1 1 353:0*1 338:0",
            "shaped 1 1 5:0*4 17:-1",
            "shapeless 351:13*4 351:4 351:1 351:1 351:15",
            "shapeless 351:7*3 351:0 351:15 351:15",
            "shapeless 351:13*3 351:4 351:1 351:9",
            "shapeless 35:15*1 351:0 35:0",
            "shapeless 35:14*1 351:1 35:0",
            "shapeless 35:13*1 351:2 35:0",
            "shapeless 35:12*1 351:3 35:0",
            "shapeless 35:11*1 351:4 35:0",
            "shapeless 35:10*1 351:5 35:0",
            "shapeless 35:9*1 351:6 35:0",
            "shapeless 35:8*1 351:7 35:0",
            "shapeless 35:7*1 351:8 35:0",
            "shapeless 35:6*1 351:9 35:0",
            "shapeless 35:5*1 351:10 35:0",
            "shapeless 35:4*1 351:11 35:0",
            "shapeless 35:3*1 351:12 35:0",
            "shapeless 35:2*1 351:13 35:0",
            "shapeless 35:1*1 351:14 35:0",
            "shapeless 35:0*1 351:15 35:0",
            "shapeless 351:9*2 351:1 351:15",
            "shapeless 351:14*2 351:1 351:11",
            "shapeless 351:10*2 351:2 351:15",
            "shapeless 351:8*2 351:0 351:15",
            "shapeless 351:7*2 351:8 351:15",
            "shapeless 351:12*2 351:4 351:15",
            "shapeless 351:6*2 351:4 351:2",
            "shapeless 351:5*2 351:4 351:1",
            "shapeless 351:13*2 351:5 351:9",
            "shapeless 351:11*2 37:0",
            "shapeless 351:1*2 38:0",
            "shapeless 351:15*3 352:0",
            "smelt 4 1:0*1",
            "smelt 12 20:0*1",
            "smelt 14 266:0*1",
            "smelt 15 265:0*1",
            "smelt 17 263:1*1",
            "smelt 56 264:0*1",
            "smelt 81 351:2*1",
            "smelt 319 320:0*1",
            "smelt 337 336:0*1",
            "smelt 349 350:0*1",
            "fuel 5 300",
            "fuel 6 100",
            "fuel 17 300",
            "fuel 25 300",
            "fuel 47 300",
            "fuel 53 300",
            "fuel 54 300",
            "fuel 58 300",
            "fuel 63 300",
            "fuel 64 300",
            "fuel 68 300",
            "fuel 72 300",
            "fuel 84 300",
            "fuel 85 300",
            "fuel 95 300",
            "fuel 96 300",
            "fuel 263 1600",
            "fuel 280 100",
            "fuel 327 20000"
    };

    public record Modern(String path, String type, String json) {
    }

    private BetaRecipes() {
    }

    public static List<Modern> modern() {
        List<Modern> out = new ArrayList<>();
        Map<String, Integer> taken = new LinkedHashMap<>();
        Map<String, Integer> smelted = new LinkedHashMap<>();
        for (String line : LINES) {
            String[] parts = line.split(" ");
            switch (parts[0]) {
                case "shaped" -> {
                    int width = Integer.parseInt(parts[1]);
                    int height = Integer.parseInt(parts[2]);
                    String[] cells = java.util.Arrays.copyOfRange(parts, 4, parts.length);
                    String json = shaped(width, height, parts[3], cells);
                    if (json != null) {
                        out.add(new Modern(name(parts[3], taken), "crafting_shaped", json));
                    }
                }
                case "shapeless" -> {
                    String json = shapeless(parts[1], java.util.Arrays.copyOfRange(parts, 2, parts.length));
                    if (json != null) {
                        out.add(new Modern(name(parts[1], taken), "crafting_shapeless", json));
                    }
                }
                case "smelt" -> out.add(new Modern(name(parts[2], smelted) + "_from_smelting", "smelting",
                        smelting(Integer.parseInt(parts[1]), parts[2])));
                default -> {
                }
            }
        }
        return out;
    }

    public static Map<String, Integer> fuel() {
        Map<String, Integer> fuel = new LinkedHashMap<>();
        for (String line : LINES) {
            String[] parts = line.split(" ");
            if (!parts[0].equals("fuel")) {
                continue;
            }
            List<String> items = BetaItems.ingredient(Integer.parseInt(parts[1]), -1);
            if (items == null) {
                continue;
            }
            for (String item : items) {
                fuel.put(item, Integer.parseInt(parts[2]));
            }
        }
        return fuel;
    }

    private static String name(String result, Map<String, Integer> taken) {
        String item = BetaItems.result(id(result), damage(result));
        String path = item.substring(item.indexOf(':') + 1);
        int n = taken.merge(path, 1, Integer::sum);
        return n == 1 ? path : path + "_" + n;
    }

    private static int id(String item) {
        return Integer.parseInt(item.substring(0, item.indexOf(':')));
    }

    private static int damage(String item) {
        int star = item.indexOf('*');
        return Integer.parseInt(item.substring(item.indexOf(':') + 1, star < 0 ? item.length() : star));
    }

    private static int count(String result) {
        return Integer.parseInt(result.substring(result.indexOf('*') + 1));
    }

    private static String result(String result) {
        String item = BetaItems.result(id(result), damage(result));
        int count = count(result);
        return "{\"id\": \"" + item + "\"" + (count == 1 ? "" : ", \"count\": " + count) + "}";
    }

    private static String ingredient(String item) {
        List<String> items = BetaItems.ingredient(id(item), damage(item));
        if (items == null || items.isEmpty()) {
            return null;
        }
        if (items.size() == 1) {
            return "\"" + items.getFirst() + "\"";
        }
        StringBuilder list = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            list.append(i == 0 ? "" : ", ").append('"').append(items.get(i)).append('"');
        }
        return list.append(']').toString();
    }

    private static String shaped(int width, int height, String result, String[] cells) {
        Map<String, Character> keys = new LinkedHashMap<>();
        StringBuilder pattern = new StringBuilder();
        for (int row = 0; row < height; row++) {
            pattern.append(row == 0 ? "" : ", ").append('"');
            for (int column = 0; column < width; column++) {
                String cell = cells[row * width + column];
                if (cell.equals("-")) {
                    pattern.append(' ');
                    continue;
                }
                Character key = keys.get(cell);
                if (key == null) {
                    key = (char) ('A' + keys.size());
                    keys.put(cell, key);
                }
                pattern.append(key);
            }
            pattern.append('"');
        }
        StringBuilder key = new StringBuilder();
        for (Map.Entry<String, Character> entry : keys.entrySet()) {
            String ingredient = ingredient(entry.getKey());
            if (ingredient == null) {
                return null;
            }
            key.append(key.isEmpty() ? "" : ", ").append('"').append(entry.getValue()).append("\": ")
                    .append(ingredient);
        }
        return "{\"type\": \"minecraft:crafting_shaped\", \"category\": \"misc\", \"pattern\": ["
                + pattern + "], \"key\": {" + key + "}, \"result\": " + result(result) + "}";
    }

    private static String shapeless(String result, String[] ingredients) {
        StringBuilder list = new StringBuilder();
        for (String item : ingredients) {
            String ingredient = ingredient(item);
            if (ingredient == null) {
                return null;
            }
            list.append(list.isEmpty() ? "" : ", ").append(ingredient);
        }
        return "{\"type\": \"minecraft:crafting_shapeless\", \"category\": \"misc\", \"ingredients\": ["
                + list + "], \"result\": " + result(result) + "}";
    }

    private static String smelting(int input, String result) {
        return "{\"type\": \"minecraft:smelting\", \"category\": \"misc\", \"cookingtime\": 200, "
                + "\"experience\": 0.0, \"ingredient\": " + ingredient(input + ":-1") + ", \"result\": "
                + result(result) + "}";
    }
}
