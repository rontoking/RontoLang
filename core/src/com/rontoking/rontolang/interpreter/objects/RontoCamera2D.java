package com.rontoking.rontolang.interpreter.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class RontoCamera2D extends RontoObject{
    OrthographicCamera camera;

    public RontoCamera2D(Interpreter interpreter){
        super();
        interpreter.addProperty(properties, "x", "double", 0);
        interpreter.addProperty(properties, "y", "double", 0);
        interpreter.addProperty(properties, "zoom", "double", 1);

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        updateCamera();
    }

    public void setBatchMatrix(SpriteBatch batch){
        updateCamera();
        batch.setProjectionMatrix(camera.combined);
    }

    public void setBatchMatrix(ShapeRenderer batch){
        updateCamera();
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    protected Reference noArgFunc(String funcName, Interpreter interpreter){
        if (funcName.equals("mount")) {
            interpreter.camera2d = this;
            updateCamera();
            setBatchMatrix(interpreter.spriteBatch);
            setBatchMatrix(interpreter.shapeRenderer);
            return new Reference(null);
        }
        return null;
    }

    @Override
    protected Reference func(Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        String funcName = child.arguments.get(0).data.toString();
        if (funcName.equals("rotate") || funcName.equals("rot")) {
            camera.rotate((float)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value));
            return null;
        }
        return null;
    }

    private void updateCamera(){
        camera.position.set((float) Variable.getNum(properties.get("x").getRef().value), (float) Variable.getNum(properties.get("y").getRef().value), 0);
        camera.zoom = (float) Variable.getNum(properties.get("zoom").getRef().value);
        camera.update();
    }
}
