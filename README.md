# Logoce - A Java Adapter API

This repository provides lighweight and low level code to allow a clean usage of the Adapter pattern with java modules. You can check the project [VSand](https://github.com/Ealrann/VSand) for a real use case.

This library is intended to be used with **Model Driven Architecture (MDA)** software developpement.

## A simple use case with Singleton Adaters

A first use case of adapters is to propose some stateless behaviour. This kind of adapter will be instanciated only once, and be reused for every model object it's compatible with.

Let's first declare a few model objects:
```
interface IVehicle

final class Car extends BasicAdaptable implements IVehicle
{
    // public to keep this exemple simple
    public boolean running = false;
}

final class HybridCar extends BasicAdaptable implements IVehicle
{
    // public to keep this exemple simple
    public boolean pistonEngineRunning = false;
    public boolean electricEngineRunning = false;
}
```

Model objects should only store data, and can be easily generated.

Then we want to create some behaviour for the car, for exemple to start it. We start to declare an IStarterAdapter, then a dedicated Starter for each type of car.

```
interface IStarterAdapter extends IAdapter
{
    void start();
}

@ModelExtender(scope = Car.class)
@Adapter(singleton = true)
final class CarStarter implements IStarterAdapter
{
    @Override
    public void start(Car car) {
        car.running = true;
    }
}

@ModelExtender(scope = HybridCar.class)
@Adapter(singleton = true)
final class HybridCarStarter implements IStarterAdapter
{
    @Override
    public void start(HybridCar car) {
        car.pistonEngineRunning = true;
        car.electricEngineRunning = true;
    }
}
```

Finally, you can start your IVehicles like that:
```
IVehicle v1 = <maybe some car>
IVehicle v2 = <maybe some hybrid car>

v1.adapt(IStarterAdapter.class).start();
v2.adapt(IStarterAdapter.class).start();
```

## A use case with Statefull Adaters

Generally, we cannot use singleton adapters because the adapter needs to keep runtime state relative to the adapted object. 

For exemple, suppose we'd like to print the car on screen, we need to store the path of the picture in model object, and manage the life-cycle of the loaded file in adapter. 

```
final class CarWithPicture extends BasicAdaptable implements IVehicle
{
    private String imagePath = "someFile.png";
}
```

We need a render Adapter. This one **cannot** be a singleton, since we'll need one loaded picture per car (we suppose the cars has different pictures).

```
interface IRenderAdapter extends IAdapter
{
    void render();
    void dispose();
}

@ModelExtender(scope = CarWithPicture.class)
@Adapter(singleton = false) // you can omit "singleton = false", it's the defauls behaviour
final class CarRenderer implements IRenderAdapter
{
    private final CarWithPicture car;
    private final File loadedPicture;
    
    // Constructor can be private, only the API will use it
    private CarStarter(CarWithPicture car)
    {
        this.car = car;
        loadedPicture = ImageUtil.loadImage(car.getImagePath());
    }
    
    public void render()
    {
        ImageUtil.printImageOnScreen(loadedPicture);
    }
    
    public void dispose()
    {
        ImageUtil.unloadImage(loadedPicture);
    }
}
```

Finally, to render your car:
```
CarWithPicture car = <...>

var renderAdapter = car.adapt(IRenderAdapter.class);
renderAdapter.render();
<...>
renderAdapter.dispose();
```

## Register your adapters to the API

To make these examples work, one last step is needed: you need to declare your adapters. To do so, your module-info must declare an `IAdapterProvider`

```
public class MyAdapters implements IAdapterProvider
{
	@Override
	public List<Class<? extends IAdapter>> classifiers()
	{
		return List.of(CarStarter.class,
					   HybridCarStarter.class,
					   CarRenderer.class;
	}

	@Override
	public MethodHandles.Lookup lookup()
	{
		return MethodHandles.lookup();
	}
}
```

Finally, in your module-info.java:
```
open module logoce.demo {
	provides IAdapterProvider with MyAdapters;
}
```

The module needs to be open, to allow the API to build your Adapters. 