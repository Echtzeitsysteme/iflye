# Collection of Metrics

This project provides a framework to collect metrics related to different applications and their performance.
It is build on top of the [micrometer](https://micrometer.io/) library, which does all the heavy lifting of collecting and exposing metrics.
In extension to the micrometer library, this project provides a wrapper for easy grouping of metrics that are collected at different components of the application but all related to the same request.
Additionally, it provides a set of metrics that are related to the Virtual Network Embedding (VNE) problem as well as some default reporters to expose the metrics for later analysis.

## Structure & Components

This project is structured into several components, each responsible for a specific aspect of the metric collection and reporting process. The main components are:

- **`MetricsManager`**: The manager is the main entry point for the metric collection process. It orchestrates the different components by wrapping the micrometer [`MeterRegistry`](https://docs.micrometer.io/micrometer/reference/concepts/registry.html) and the [`ObservationRegistry`](https://docs.micrometer.io/micrometer/reference/observation/introduction.html). It is used to start observations and to register metrics.
- **`Handler`**: A handler, implementing the `HasMetric` interface, is responsible for collecting certain metrics. As an [`ObservationHandler`](https://docs.micrometer.io/micrometer/reference/observation/components.html#micrometer-observation-handler), it could react to different events during the observation lifecycle. Each handler is free to implement its own logic to collect metrics, including the related format options to expose the metrics during reporting.
- **`Reporter`**: Collected metrics are exposed via different channels using one reporter per channel. The reporter is responsible for formatting the metrics appropriately and sending them to the desired destination. They implement the `Reporter` interface and have to extend a [`Registry`](https://docs.micrometer.io/micrometer/reference/concepts/registry.html), which will be part of a `CompositeRegistry` managed by the `MetricsManager`.

The general process of metric collection and reporting is as follows:

1. The `MetricsManager` is initialized with a set of handlers and reporters.
2. The code that should be observed is wrapped in an `Observation` using the `MetricsManager`.
3. The `MetricsManager` starts the observation, executes the code, and notifies the handlers about the start of the observation.
4. Once the observed code is finished, the `MetricsManager` notifies the handlers about the end of the observation. This also happens in case of an exception.
5. The handlers collect the metrics using the provided `MeterRegistry`.
6. The reporters could be `flush`ed after each request, but at latest before the application is stopped. This will ensure that all collected metrics are sent to the desired destination.

## Usage

To use the metrics framework, you need to create a `MetricsManager` instance and register your handlers and reporters. The special `MetricsManager.Default` class is already preconfigured with a set of default handlers and reporters for the VNE problem domain.

A runnable example is provided in the [`MetricsExample`](src/metrics/MetricsExample.java) class. It shows how to use the `MetricsManager` to collect and report metrics.

## `MetricsManager`

The `MetricsManager` is the main entry point for the metric collection process. It orchestrates the different components by wrapping the micrometer [`MeterRegistry`](https://docs.micrometer.io/micrometer/reference/concepts/registry.html) and the [`ObservationRegistry`](https://docs.micrometer.io/micrometer/reference/observation/introduction.html).

The `MetricsManager` is first initialized by simply creating a new instance: `MetricsManager metricsManager = new MetricsManager();`. This instance is then used to register [handlers](#handlers) and [reporters](#reporters). The special `MetricsManager.Default` class is already preconfigured with a set of default handlers and reporters for the VNE problem domain.

Subsequent access to the `MetricsManager` is done via the static method `MetricsManager.getInstance()`. This will return the most recent instance of the `MetricsManager`.

### Tags

Tags are used to label metrics with additional meta-information which comes in handy when analyzing the metrics. Tags are key-value pairs that can be used to group and filter metrics. The `MetricsManager` handles a set of tags per instance, which could be added to the current instance in several ways using the `addTags` method:

```java
// Add valid tags
metricsManager.addTags("tag1", "value1", "tag2", "value2"); // Each tag is a key-value pair, therefore an even number of arguments is required
// Also valid
metricsManager.addTags(Tag.of("tag1", "value1"), Tag.of("tag2", "value2"));
// Still valid
metricsManager.addTags(Tags.of("tag1", "value1", "tag2", "value2")); // Tags is a wrapper for a list of tags
```

Each started observation will inherit the current tags from the `MetricsManager` instance. You can read in the paragraph on [Nesting & Multi-threading](#Nesting--Multi-threading) how to create new instances of the `MetricsManager` with additional tags.

### Observations

An observation is the collection of metrics related to a certain execution context. A new observation is created by calling the `MetricsManager.observe()` method, which takes a name for the observation and a `Runnable` or `Callable` as the code to be executed. Optionally, an [`Observation.Context`](https://docs.micrometer.io/micrometer/reference/observation/introduction.html#micrometer-observation-glossary) can be provided to store additional information related to the observation. The `Runnable` or `Callable` will be executed within the context of the observation. The `MetricsManager` will automatically start and stop the observation, and notify the handlers about the start and end of the observation. The handlers can then collect the metrics using the provided `MeterRegistry`.

The `MetricsManager` provides a set of specialized `Observation.Context` for the VNE problem domain. The base `Context` provides the substrate network, the current virtual network, the instance of the `MetricsManager` that started the observation, and, if the observation was started with a `Callable`, the result of the callable once it is available. Inheriting from this base context, several contexts provide additional information about the current step to be observed:

```
Context
    ↳ VnetEmbeddingContext
        ↳ VnetRootContext
    ↳ PhaseContext
        ↳ StageContext
            ↳ PrepareStageContext
            ↳ ExecuteStageContext
        ↳ StepContext
            ↳ IlpStepContext
            ↳ PmStepContext
            ↳ DeployStepContext
```

The `VnetEmbeddingContext` is the base context for all observations related to the embedding of a virtual network, which is specialized by the `VnetRootContext` for the root VNR. Each of the phase contexts provides additional information about the respective phase, stage, or step to be observed. Please refer to the individual [context classes](src/metrics/manager/Context.java) for more information.

### Nesting & Multi-threading

The `MetricsManager` is designed as a multiton to be nestable. Thereby, once a new `MetricsManager` is created, it will be used for all subsequent calls to `MetricsManager.getInstance()`. Once the execution context of the `MetricsManager` is finished, it is important to `close()` it to restore the previous instance. This is important to ensure that the correct `MetricsManager` instance is used for the next call to `MetricsManager.getInstance()`. To simplify the management of the most recent context, `MetricsManager` implements the `AutoCloseable` interface. This allows the `MetricsManager` to be used in a try-with-resources statement, which will automatically close the instance when it is no longer needed.

New instances could be created by calling `MetricsManager.getInstance().clone()` or `MetricsManager.getInstance().withTags()`. Instances created this way inherit all handlers, reporters, registries, and tags from its parent instance. The `withTags()` method additionally appends the given tags to the existing tags. Adding tags to an existing instance will only affect new child instances but not propagate to already existing childs. It is important to note that adding handlers or reporters _will_ propagate globally to all instances that originated from the same root instance, no matter where they forked from the inheritance tree.

It is recommended to create new instances using the following template:

```java
// Create a new instance, add tags, and close it automatically
try (MetricsManager metricsManager = MetricsManager.getInstance().withTags("tag1", "value1")) {
    // do something
}
```

This will create a new instance of the `MetricsManager` with the given tags and automatically close it when the execution context is finished. This ensures that the correct `MetricsManager` instance is used for the next call to `MetricsManager.getInstance()`.

> It is not recommended to create new instances using the constructor. However, for the sake of completeness, this will push a new instance to the stack of instances, that is decoupled from the other instances and won't inherit any reporters, registries, or tags. Against this, it will inherit the handlers. The creation of a new instance using the constructor is not guaranteed to be thread-safe or side-effect free!

The `MetricsManager` is thread-safe as such as each thread maintains its own set of multitons. To access the `MetricsManager` from a different thread, this requires it to be prepared for the new thread. The `MetricsManager` is compatible with the common `Runnable` and `Callable` interfaces by providing a `wrap` method. The `wrap` method returns a new `Runnable` or `Callable` respectively, that will create a new instance of the `MetricsManager` for the thread and inherit all handlers, reporters, registries, and tags from the current instance. The instance will be closed automatically once the thread execution finished. Optionally, the `wrap` method could be provided with tags just like the `withTags` method. This allows the `MetricsManager` to be used in a multi-threaded environment without any issues.

Because it's a common pattern to first `wrap()` a new instance and then `observe()` it, the `MetricsManager` provides a convenience method `wrapObserve()` that combines both steps. It takes all combinations of arguments that could be passed to each of the two methods and returns a new `Runnable` or `Callable` that will create a new instance of the `MetricsManager` for the thread, call the `observe()` method on it, run the code, and close the instance automatically once the thread execution finished.

## Handlers

Handlers are responsible for collecting metrics during the observation lifecycle. They implement the `HasMetric` interface and can react to different events during the observation lifecycle. Each handler is free to implement its own logic to collect metrics, including the related format options to expose the metrics during reporting.

The event lifecycle of an observation is as follows:

- `onStart()`: Called when the observation is started. This is where the handler can initialize any resources needed for the observation, e.g., start a timer.
- `onError()`: Called when an error occurs during the observation. This is where the handler can collect metrics related to the error.
- `onStop()`: Called when the observation is stopped. This is where the handler can collect metrics related to the observation and clean up any resources used during the observation.
- `onEvent()`: Called when an event occurs during the observation. An event is defined by the code to be executed and explicitly triggered by calling `MetricsManager.getInstance().event()`. With the `onEvent()` hook, the handler can collect metrics related to the event.
- `close()`: This method is only available if the handler implements the `AutoCloseable` interface. It is called when the `MetricsManager` is closed

Each handler may define which contexts it is able to handle. The `supportsContext()` method is used to check if the handler can handle the given context. If the context is not supported, the handler will not be notified about the observation lifecycle events. This allows handlers to be selective about which observations they want to collect metrics for.

During the observation lifecycle, the handler can collect metrics using the provided `MeterRegistry`. The `MeterRegistry` is a micrometer registry that is used to collect and expose metrics and the `MeterRegistry` instance to use will be provided by the `MetricsManager`. The handler can use the `MeterRegistry` to create new meters, timers, and other metrics as needed and described in the [micrometer documentation](https://docs.micrometer.io/micrometer/reference/observation/components.html#micrometer-observation-handler-example). It is the responsibility of each handler to name the metrics it collects appropriately and to use the correct tags to label them. Therefore, the `MetricsManager` provides the tags as a set of key-value pairs as part of the `Context` object. Please refer to the provided implementations on how to tag the metrics appropriately.

Additionally to the metric collection, the handler can provide additional information on how to format the metrics for reporting. By default, the [reporters](#reporters) will report all metrics measurements that are registered in the `MeterRegistry` by their corresponding name. However, the handler can provide a custom format by providing a list of `MetricTransformer` instances with the `getProvidedMeters()` method. Each `MetricTransformer` can transform the meter with regards to its name, the measurements to report as well as the (`double`) values to report. Additionally, if a reporter provides a custom format option, the handler can implement a custom format that is specific to the reporting channel.

There are some default handlers provided in the [`metrics.handler`](src/metrics/handler) package:

- [`CounterHandler`](src/metrics/handler/CounterHandler.java): A handler that simply counts the number of `VnetEmbeddingContext` observations.
- [`EmbeddedNetworkHandler`](src/metrics/handler/EmbeddedNetworkHandler.java): A handler that collects metrics on the quality of the embedded networks. The different metrics are available for individual use in the `metrics.embedding` package.
- [`ErrorHandler`](src/metrics/handler/ErrorHandler.java): A handler that adds an `exception` tag with the exception message or the exception class name to the metrics, if any exception occurred during the observation.
- [`MemoryHandler`](src/metrics/handler/MemoryHandler.java): A handler that collects metrics on the memory usage of the application. It starts a sampling thread and gathers a set of metrics related to the heap and non-heap memory usage on a fixed schedule.
- [`ThreadHandler`](src/metrics/handler/ThreadHandler.java): A handler that collects metrics on the thread usage of the application. It starts a sampling thread and gathers a set of metrics related to the thread usage on a fixed schedule.
- [`TimingHandler`](src/metrics/handler/TimeHandler.java): A handler that runs a stop clock to measure the time taken for the observation.

The instance of a handler can be registered by calling the `metricsManager.addMeter(handler)` method.

## Reporters

Reporters are responsible for exposing the collected metrics to a specific destination. They implement the `Reporter` interface and have to extend a [`MeterRegistry`](https://docs.micrometer.io/micrometer/reference/concepts/registry.html), which will be part of a `CompositeRegistry` managed by the `MetricsManager`. The reporters are responsible for formatting the metrics appropriately and sending them to the desired destination.

The event lifecycle of a reporter is as follows:

- `initialized()`: Once the `MetricsManager` is initialized with all handlers, reporters, and tags, the `initialized()` method is called on each reporter. This enables them to prepare for incoming metrics, e.g., by creating an initial entry in a database.
- `flush()`: After a closed set of observations that belong to the same request, the `flush()` method is called on each reporter to send the collected metrics to the desired destination. It is up to the reporter if they want to send their report on every flush or at a later stage.
- `conclude()`: During or after observations, the `conclude()` method can be invoked to provide a wrap-up of the collected metrics. This method needs to be idempotent if the reporter uses it instead of `flush()`.
- `close()`: The `close()` method is called when the root `MetricsManager` instance is closed. This is where the reporter can clean up any resources used and send any remaining metrics to the desired destination.

> The abstract [`GroupedReporter`](src/metrics/reporter/GroupedReporter.java) and its specializations [`GroupByTagsReporter`](src/metrics/reporter/GroupByTagsReporter.java) and [`GroupByTagValueReporter`](src/metrics/reporter/GroupByTagValueReporter.java) already implements the lifecycle methods partially for common metric handling scenarios. Please refer to their respective documentation for more information.

The default set of reporters is provided in the [`metrics.reporter`](src/metrics/reporter) package:

- [`CsvReporter`](src/metrics/reporter/CsvReporter.java): A reporter that writes the collected metrics to a CSV file. The file name can be provided as a parameter to the constructor. The order of the columns can be specified individually as well as the tags that should be included in the report. The reporter will append the metrics to the file if it exists, so it is recommended to use a new file for each run. Metrics will be grouped by the value of the `"series group uuid"` tag.
- [`NotionReporter`](src/metrics/reporter/NotionReporter.java): A reporter that writes the collected metrics to a Notion database. The database ID and the Notion API token can be provided as parameters to the constructor. The reporter will create a new page in the database for each entry grouped by the value of the `"series group uuid"` tag. An optional series database ID could be provided to group all metrics of one run during initialization. Only metrics that implement the `NotionMeter` metric transformer will be considered for the report, as Notion requires addtional information on the type of reported metrics.
- [`TextSummaryReporter`](src/metrics/reporter/TextSummaryReporter.java): A reporter that listens for any metrics that implement the `AggregatingMeter` metric transformer. Metrics that provide an aggregation type will be aggregated during the application run and a summary of the current aggregation values will be printed on each call to `conclude()`. Metrics are grouped by metric name, without any tags. The reporter will print the summary to the console by default, but this can be changed by providing a `Consumer` to the constructor.

The instance of a handler can be registered by calling the `metricsManager.addReporter(reporter)` method.
