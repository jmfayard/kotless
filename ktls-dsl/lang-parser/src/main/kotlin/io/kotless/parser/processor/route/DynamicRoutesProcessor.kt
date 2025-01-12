package io.kotless.parser.processor.route

import io.kotless.*
import io.kotless.Webapp.ApiGateway
import io.kotless.Webapp.Events
import io.kotless.dsl.kotlessLambdaEntrypoint
import io.kotless.dsl.lang.http.Get
import io.kotless.dsl.lang.http.Post
import io.kotless.parser.processor.AnnotationProcessor
import io.kotless.parser.processor.ProcessorContext
import io.kotless.parser.processor.action.GlobalActionsProcessor
import io.kotless.parser.processor.permission.PermissionsProcessor
import io.kotless.parser.utils.psi.annotation.getURIPath
import io.kotless.utils.TypedStorage
import io.kotless.utils.everyNMinutes
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.BindingContext

internal object DynamicRoutesProcessor : AnnotationProcessor<Unit>() {
    override val annotations = setOf(Get::class, Post::class)

    override fun mayRun(context: ProcessorContext) = context.output.check(GlobalActionsProcessor)

    override fun process(files: Set<KtFile>, binding: BindingContext, context: ProcessorContext) {
        val permissions = context.output.get(GlobalActionsProcessor).permissions

        processFunctions(files, binding) { func, entry, klass ->
            val routePermissions = PermissionsProcessor.process(func, binding) + permissions

            val name = prepareFunctionName(func, context.lambda.packages)

            val key = TypedStorage.Key<Lambda>()
            val function = Lambda(name, context.jar, Lambda.Entrypoint(kotlessLambdaEntrypoint, emptySet()), context.lambda, routePermissions)

            val (routeType, pathProperty) = when (klass) {
                Get::class -> HttpMethod.GET to Get::path
                Post::class -> HttpMethod.POST to Post::path
                else -> error("Not supported class $entry")
            }

            val path = entry.getURIPath(binding, pathProperty)!!

            context.resources.register(key, function)
            context.routes.register(ApiGateway.DynamicRoute(routeType, path, key))
            if (context.config.optimization.autowarm.enable) {
                context.events.register(Events.Scheduled(name, everyNMinutes(context.config.optimization.autowarm.minutes), ScheduledEventType.Autowarm, key))
            }
        }
    }

    private fun prepareFunctionName(route: KtNamedFunction, packages: Set<String>): String = route.fqName!!.asString().let { fqName ->
        val pckg = packages.find { fqName.startsWith("$it.") }
        if (pckg != null) {
            fqName.drop("$pckg.".length)
        } else {
            fqName
        }
    }
}
