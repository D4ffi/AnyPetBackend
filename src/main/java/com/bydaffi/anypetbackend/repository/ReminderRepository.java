package com.bydaffi.anypetbackend.repository;

import org.springframework.stereotype.Repository;

/**
 * @deprecated This repository is no longer used. Reminders are now stored exclusively in Firebase Firestore.
 * The ReminderService handles all Firestore operations directly.
 * This class is kept to avoid breaking changes but should not be used.
 */
@Deprecated
@Repository
public interface ReminderRepository {
    // This repository is deprecated and no longer in use
    // All reminder operations are now handled directly through Firebase Firestore
}
