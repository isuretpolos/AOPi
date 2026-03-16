# Drawings
````java
ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
float screenHeight = Gdx.graphics.getHeight();
float screenWidth = Gdx.graphics.getWidth();

batch.begin();
String text = "Das ist ein Test und noch ein Test";
layout.setText(font, text);
font.draw(batch, text, 10, 100 + font.getXHeight());
batch.end();

shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
shapeRenderer.line(10,90, layout.width, 90);
shapeRenderer.end();

/*batch.begin();
//batch.draw(image, 140, 210);

shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
shapeRenderer.setColor(1, 0, 0, 1); // rot
shapeRenderer.circle(300, 300, 50);
shapeRenderer.end();

shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
shapeRenderer.setColor(0, 1, 0, 1); // rot
shapeRenderer.line(100, 100, 300, 200);
shapeRenderer.rect(50, 50, 200, 120);
shapeRenderer.end();

batch.end();*/
````
