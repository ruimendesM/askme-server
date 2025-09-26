package com.ruimendes.askme.domain.exception

class SamePasswordException: RuntimeException("The new password cannot be the same as the old password")