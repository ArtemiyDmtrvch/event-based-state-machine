## Welcome aboard!

**Be calm for your project — let it drive on rails!**

<img src="https://www.railjournal.com/images/China-Olympic-EMU-LARGE.jpg" alt="Let your project drive on rails!" height="55%" width="55%"/>

**flow-architecture** is a versatile and easy-to-use architecture component that solves very common problems of Android 
development. Any Android project has many logical components (Activities, Fragments, Services, Views, etc.) that 
interact with each other in many different ways. But at the same time, they all perform one common business process, 
which is mixed with a bunch of technical logic. Because of this:
- it becomes difficult to track the sequence of actions
- even minor changes to one of the components may cause the unexpected behavior of the other
- if the project is large, you will waste a lot of time to find the place you need

**flow-architecture** will save you from all these problems by bringing out the flowchart of your business process, which is 
designed to be understood by all. Take a look:

``` kotlin
whenEventOccurs<AppStarted> {
    performAction(LoadData())
}
whenEventOccurs<DataLoaded> { event ->
    performAction(ShowData(event.data))
}
```
Even a person far from programming can figure it out, right? 

Then you create your logical components that are just the performers of your flowchart. Therefore, they work 
independently of each other and can be considered as plug-ins that are easy to edit or replace. And so trivially 
they are associated with the flowchart:

``` kotlin
//Component №1

fun main() {
    eventOccurred(AppStarted())
}

override fun performAction(action: Flow.Action) {
    when (action) {
        is ShowData -> welcomeTextView.text = action.data
    }
}
```
``` kotlin
//Component №2

override fun performAction(action: Flow.Action) {
    when (action) {
        is LoadData -> eventOccurred(DataLoaded("Hello world!"))
    }
}
```

With this approach:
- you think much less when writing the business logic of your app, especially if you have an analytical flowchart
- regardless of your experience, you can easily navigate the project of any complexity
- you can make any changes to the logical components, replace them, and not worry about the consequences

## Install it and let's move on

1. Add the JitPack repository in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
2. Add the dependency:
```
dependencies {
    implementation 'com.github.ArtemiyDmtrvch:flow-architecture:0+'
}
```

*The documentation is not yet fully written. You may understand this project by looking at [the demo application]( https://github.com/ArtemiyDmtrvch/flow-architecture-example).*
