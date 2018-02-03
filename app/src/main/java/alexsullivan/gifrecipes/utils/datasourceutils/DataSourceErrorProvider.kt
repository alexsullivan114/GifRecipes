package alexsullivan.gifrecipes.utils.datasourceutils

import io.reactivex.Flowable

interface DataSourceErrorProvider {
  val initialLoadingFlowable: Flowable<Boolean>
  val futherLoadingFlowable: Flowable<Boolean>
  val initialLoadingErrorFlowable: Flowable<Throwable>
  val futherLoadingErrorFlowable: Flowable<Throwable>
}