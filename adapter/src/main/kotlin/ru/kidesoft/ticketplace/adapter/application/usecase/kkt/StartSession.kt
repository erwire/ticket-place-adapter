package ru.kidesoft.ticketplace.adapter.application.usecase.kkt

import ru.kidesoft.ticketplace.adapter.application.port.*
import ru.kidesoft.ticketplace.adapter.application.presenter.SceneManager
import ru.kidesoft.ticketplace.adapter.application.usecase._Usecase
import ru.kidesoft.ticketplace.adapter.domain.ShiftState

class StartKktSession(val databasePort : DatabasePort, private val kktPortFactory: KktPortFactory) : _Usecase<StartKktSession.Input, StartKktSession.Output>() {
    class Input : _Usecase.Input
    class Output : _Usecase.Output

    override suspend fun invoke(inputValues: Input?, sceneManager: SceneManager?): Output {

        val profile = databasePort.getProfile().getCurrentProfile()?: throw NullPointerException("Profile can't be null")

        val setting = databasePort.getSetting().getByCurrentUser() ?: throw NullPointerException("Setting can't be null")

        val kktInstance = kktPortFactory.getInstance(kktType = KktType.ATOL, profile.loginId) ?: kktPortFactory.createInstance(KktType.ATOL, setting.kkt, profile.loginId)


        if (!kktInstance.getConnection()) kktInstance.openConnection()

        when(kktInstance.getShiftState()) {
            ShiftState.CLOSED -> kktInstance.openShift(profile.cashier)
            ShiftState.EXPIRED -> {
                kktInstance.closeShift(profile.cashier)
                kktInstance.openShift(profile.cashier)
            }
            else -> {}
        }

        return Output()
    }

}