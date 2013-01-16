package com.tvganesh.blob;

/* 
 * Developed by Tinniam V Ganesh, 16 Jan 2013
 * Uses Box2D physics engine and AndEngine
 * Based on http://gwtbox2d.appspot.com/ BlobJoint demo
 */


import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import android.hardware.SensorManager;
import android.util.Log;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;


public class Blob extends SimpleBaseGameActivity implements IAccelerationListener {
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	public static final float PIXEL_TO_METER_RATIO_DEFAULT = 32.0f;
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	
	
	
	
   
    
    private Scene mScene;
    
    private PhysicsWorld mPhysicsWorld;
	private ITiledTextureRegion mCircleFaceTextureRegion;
	private TextureRegion mBrickTextureRegion;
	
    
    private static FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(50f, 0.0f, 0.5f);
	public EngineOptions onCreateEngineOptions() {
		
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}
	
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");	
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 164, 82, TextureOptions.BILINEAR);		
		
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_circle_tiled.png", 0, 0, 2, 1); // 64x32
		this.mBitmapTextureAtlas.load();		
		
		this.mBrickTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "brick.png",64, 32);
		this.mBitmapTextureAtlas.load();		
	

	}
	
	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		// Create Blob scene
		this.initBlob(mScene);
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		return mScene;		
		
	}
	
	public void initBlob(Scene mScene){
		
		
		Sprite brick;
		Body brickBody;
		
		final Body circleBody[] = new Body[20];
		final Line connectionLine[] = new Line[20];
		final AnimatedSprite circle[] = new AnimatedSprite[20];
		
	
		
		//Create the floor,ceiling and walls
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.0f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		
		DistanceJointDef distanceJoint = new DistanceJointDef();
		float cx = 0.0f;
		float cy = 10.0f;
		float rx = 5.0f;
		float ry = 5.0f;
		final int nBodies = 20;
		float bodyRadius = 0.5f;
		final float PI=3.1415f;
		float centers[][] = new float[20][2];
		
		// Add 20 circle bodies around an ellipse
		for (int i=0; i<nBodies; ++i) {
			FIXTURE_DEF = PhysicsFactory.createFixtureDef(30f, 0.5f, 0.5f);
		    
			float lineWidth = 5.0f;
			
			//Ellipse : x= a cos (theta) y = b sin (theta)
			float angle = (2 * PI* i)/20;
			float x = cx + rx * (float)Math.sin(angle);
			float y = cy + ry * (float)Math.cos(angle);
			
			// Scale appropriately for screen size
			float x1 = (x + 10) * 30;
			float y1 = y * 20;
			centers[i][0] = x1;
			centers[i][1] = y1;
			
		     
			  Vector2 v1 = new Vector2(x1,y1);
			  final VertexBufferObjectManager vb = this.getVertexBufferObjectManager();
			  circle[i] = new AnimatedSprite(x1, y1, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager());
			  circleBody[i] = PhysicsFactory.createCircleBody(this.mPhysicsWorld, circle[i], BodyType.DynamicBody, FIXTURE_DEF);
			  
			  // Join adjacent bodies
			  if(i > 0) {
				     connectionLine[i] = new Line(centers[i][0],centers[i][1],centers[i-1][0],centers[i-1][1],lineWidth,this.getVertexBufferObjectManager());
				     connectionLine[i].setColor(0.0f,0.0f,1.0f);
				     this.mScene.attachChild(connectionLine[i]);	 
				     
			  }
			  
			  // Join the first body with the last body
			  if(i == 19){
				  connectionLine[0] = new Line(centers[0][0],centers[0][1],centers[19][0],centers[19][1],lineWidth,this.getVertexBufferObjectManager());
				  connectionLine[0].setColor(.0f,.0f,1.0f);
				  this.mScene.attachChild(connectionLine[0]);	
			  }
			 
			  // Update connection line so that the line moves along with the body
			  this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(circle[i], circleBody[i], true, true) {
				  @Override
					public void onUpdate(final float pSecondsElapsed) {
						super.onUpdate(pSecondsElapsed);
						for(int i=1;i < nBodies;i++) {
							connectionLine[i].setPosition(circle[i].getX(),circle[i].getY(),circle[i-1].getX(),circle[i-1].getY());
						
						}
						connectionLine[0].setPosition(circle[0].getX(),circle[0].getY(),circle[19].getX(),circle[19].getY());
			        }
			  }		  
					  
			);
			  
			  this.mScene.attachChild(circle[i]);  
			  
		}	
		//  Create a distanceJoint between every other day
		for(int i= 0;i < nBodies-1; i++)  {
			
		   for(int j=i+1; j< nBodies ; j++){
			   Vector2 v1 = new Vector2(centers[i][0]/PIXEL_TO_METER_RATIO_DEFAULT,centers[i][1]/PIXEL_TO_METER_RATIO_DEFAULT);
			   Vector2 v2 = new Vector2(centers[j][0]/PIXEL_TO_METER_RATIO_DEFAULT,centers[j][1]/PIXEL_TO_METER_RATIO_DEFAULT);
			   distanceJoint.initialize(circleBody[i], circleBody[(j)], v1, v2);		   
			   distanceJoint.collideConnected = true;
			   distanceJoint.dampingRatio = 50.0f;
			   distanceJoint.frequencyHz = 0.5f;
			   this.mPhysicsWorld.createJoint(distanceJoint);
		   }
		}
		
		  // Create a brick above the blob. Make it slightly heavy
		  FIXTURE_DEF = PhysicsFactory.createFixtureDef(50f, 0.5f, 0.5f);
		  brick = new Sprite(200,10, this.mBrickTextureRegion, this.getVertexBufferObjectManager());			
		  brickBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, brick, BodyType.DynamicBody, FIXTURE_DEF);	  
		  
		  this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(brick, brickBody, true, true));
		  this.mScene.attachChild(brick);			
		
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
	}


	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
		
	}


	@Override
	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerationSensor(this);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}
	
	

}
