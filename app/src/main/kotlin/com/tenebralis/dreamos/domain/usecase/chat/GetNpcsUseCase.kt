package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.domain.model.Npc
import com.tenebralis.dreamos.domain.repository.NpcRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetNpcsUseCase @Inject constructor(
    private val repository: NpcRepository
) {

    suspend operator fun invoke(): Result<List<Npc>> = repository.getByUser().first()
}
