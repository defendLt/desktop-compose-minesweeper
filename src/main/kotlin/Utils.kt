fun stringResource(type: StringValueType) =
    if (System.getProperty("user.language").equals("ru")) StringResRu.get(type)
    else StringResEn.get(type)